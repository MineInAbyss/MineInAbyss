package com.mineinabyss.features.cinematics

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.actionContext
import com.mineinabyss.features.helpers.freeze
import com.mineinabyss.features.helpers.hideHands
import com.mineinabyss.features.helpers.holdFadeTitle
import com.mineinabyss.features.helpers.isInCinematicOrTransition
import com.mineinabyss.features.helpers.lockCameraDistance
import com.mineinabyss.features.helpers.postEffectSteps
import com.mineinabyss.features.helpers.releaseCameraDistance
import com.mineinabyss.features.helpers.releaseFadeTitle
import com.mineinabyss.features.helpers.removePostEffects
import com.mineinabyss.features.helpers.resetCamera
import com.mineinabyss.features.helpers.runIn
import com.mineinabyss.features.helpers.showPostEffectStep
import com.mineinabyss.features.helpers.slidePostEffects
import com.mineinabyss.features.helpers.spawnStand
import com.mineinabyss.features.helpers.unfreeze
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*
import kotlin.time.Duration

private const val BAR_STEPS = 4
private val BARS = Key.key("mineinabyss", "cinematic/bars")
private val STALE_EFFECT_PREFIXES = listOf("cinematic/", "${DEFAULT_FADE.value()}_")
private val CAMERA_KEY = NamespacedKey("mineinabyss", "cinematic/camera")
private val FREEZE_KEY = NamespacedKey("mineinabyss", "cinematic/freeze")
private const val SKIP_SETTLE_TICKS = 10

class Cinematics internal constructor(private val registry: CinematicRegistry) : Listener {
    private val sessions = mutableMapOf<UUID, CinematicSession>()

    fun isPlaying(player: Player) = player.uniqueId in sessions

    fun play(player: Player, key: PrefabKey, anchor: CinematicAnchor, environment: Map<String, Any?> = emptyMap()): Boolean {
        val scene = registry[key] ?: return false.also { abyss.logger.w("Unknown cinematic $key") }
        if (player.isInCinematicOrTransition() || player.isInsideVehicle || player.isDead) return false
        dialogSessions?.close(player)

        val origin = player.location
        val seat = spawnStand(player, origin) { it.isMarker = true }
        val session = CinematicSession(player, key, scene, anchor, environment, origin, seat)
        sessions[player.uniqueId] = session
        seat.addPassenger(player)
        player.freeze(FREEZE_KEY)
        player.hideHands()
        player.lockCameraDistance(CAMERA_KEY)
        Sections.setCanMove(player, false)
        if (scene.hidePlayer) Bukkit.getOnlinePlayers().filter { it != player }.forEach { it.hidePlayer(abyss, player) }

        session.job = abyss.launch {
            try {
                coroutineScope { run(session) }
                session.completed = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: SceneFailure) {
                abyss.logger.w("Cinematic $key stopped: ${e.message}")
            } catch (e: Exception) {
                abyss.logger.e("Cinematic $key failed: ${e.message}", e)
            } finally {
                finish(session)
            }
        }
        return true
    }

    fun skip(player: Player) {
        val session = sessions[player.uniqueId]?.takeUnless { it.skipRequested } ?: return
        session.skipRequested = true
        val fade = session.scene.skipFade
        player.holdFadeTitle(fade.title, fade.fadeIn)
        abyss.launch {
            delay(fade.fadeIn)
            session.skipping = true
        }
    }

    fun stop(player: Player, returnPlayer: Boolean = true) {
        val session = sessions[player.uniqueId] ?: return
        restore(session, returnPlayer)
        session.job.cancel()
    }

    fun stopAll() {
        sessions.values.toList().forEach { stop(it.player) }
    }

    private fun finish(session: CinematicSession) {
        val ended = !session.restored && session.completed
        restore(session)
        val player = session.player
        if (session.skipRequested && player.isOnline) abyss.launch {
            // Chunks around where the scene ended reach the client before the screen clears
            delay(SKIP_SETTLE_TICKS.ticks)
            val fade = session.scene.skipFade
            player.releaseFadeTitle(fade.title, fade.fadeOut)
        }
        if (ended && player.isOnline) session.scene.onEnd?.runIn(actionContext(player, session.environment))
    }

    private suspend fun CoroutineScope.run(session: CinematicSession) {
        for (step in session.scene.steps) {
            if (step.parallel) launch { perform(session, step) }
            else perform(session, step)
        }
    }

    private suspend fun perform(session: CinematicSession, step: CinematicStep) {
        when (step) {
            is WaitStep -> session.pause(step.duration)
            is BarsStep -> slide(session, BARS, BAR_STEPS, step.show, step.duration)
            is FadeStep -> slide(session, step.effect, step.effectSteps, step.darken, step.duration)
            is MannequinStep -> session.spawnMannequin(step)
            is WalkStep -> session.walk(step)
            is CameraStep -> session.camera(step)
            is GondolaStep -> session.gondola(step)
            is RemoveStep -> session.remove(step.id)
            is SoundStep -> if (!session.skipping) session.player.playSound(Sound.sound(step.sound, Sound.Source.MASTER, step.volume, step.pitch))
            is ActionsStep -> step.actions.runIn(actionContext(session.player, session.environment))
        }
    }

    private suspend fun slide(session: CinematicSession, base: Key, steps: Int, on: Boolean, duration: Duration) {
        val keys = postEffectSteps(base, steps.coerceAtLeast(1))
        session.effects += keys
        session.player.slidePostEffects(keys, on, duration) { session.pause(it) }
    }

    private fun restore(session: CinematicSession, returnPlayer: Boolean = true) {
        if (session.restored) return
        session.restored = true
        sessions.remove(session.player.uniqueId)
        val player = session.player
        val end = if (!session.completed) null
        else session.scene.endAt?.let { id -> session.riders[id]?.let { landing(it) } ?: session.mannequins[id]?.location }
            ?: session.carried?.let { landing(it) }
        if (player.isOnline) player.resetCamera()
        player.showPostEffectStep(session.effects.toList(), null)
        session.seat.removePassenger(player)
        session.seat.remove()
        if (returnPlayer && player.isOnline && !player.isDead) player.teleport(end ?: session.origin)
        session.riders.values.forEach { it.riser.remove() }
        session.mannequins.values.forEach(Entity::remove)
        session.gondolas.values.forEach(CinematicGondola::remove)
        session.camera?.remove()
        session.chunkTickets.forEach { it.removePluginChunkTicket(abyss) }
        player.releaseCameraDistance(CAMERA_KEY)
        showBody(player)
        Sections.setCanMove(player, true)
        player.unfreeze(FREEZE_KEY)
        if (player.isOnline) player.updateInventory()
        Bukkit.getOnlinePlayers().forEach { it.showPlayer(abyss, player) }
    }

    @EventHandler
    fun EntityDismountEvent.onDismount() {
        val session = sessions[entity.uniqueId]?.takeIf { dismounted === it.seat && !it.restored && !it.relocating } ?: return
        isCancelled = true
        if (session.scene.skippable) skip(session.player)
    }

    // A teleport from elsewhere would otherwise dismount the player, which skips the scene and leaves them on the seat
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerTeleportEvent.onTeleport() {
        if (sessions[player.uniqueId]?.relocating == false) stop(player, returnPlayer = false)
    }

    // A creative client writes the transparent items it was sent back into the player's real inventory
    @EventHandler
    fun InventoryCreativeEvent.onCreativeSlot() {
        if (whoClicked.uniqueId in sessions) isCancelled = true
    }

    @EventHandler
    fun EntityDamageEvent.onDamage() {
        if (entity.uniqueId in sessions) isCancelled = true
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        stop(player)
    }

    // Post effects and invisibility are saved with the player, so a scene cut off by a crash is undone on join
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.removePostEffects(STALE_EFFECT_PREFIXES)
        showBody(player)
        sessions.values.filter { it.scene.hidePlayer }.forEach { player.hidePlayer(abyss, it.player) }
    }
}
