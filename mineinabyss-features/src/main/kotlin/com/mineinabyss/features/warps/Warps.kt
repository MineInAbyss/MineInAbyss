package com.mineinabyss.features.warps

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.cinematics.isInCinematic
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.Cost
import com.mineinabyss.features.helpers.freezeWalkSpeed
import com.mineinabyss.features.helpers.holdFadeTitle
import com.mineinabyss.features.helpers.releaseFadeTitle
import com.mineinabyss.features.helpers.restoreWalkSpeed
import com.mineinabyss.features.helpers.showPostEffectStep
import com.mineinabyss.features.helpers.slidePostEffects
import com.mineinabyss.idofront.messaging.error
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.time.Duration

private val FREEZE_KEY = NamespacedKey("mineinabyss", "warp/freeze")
private val EFFECTS_KEY = NamespacedKey("mineinabyss", "warp/post_effects")

class Warps(val config: WarpsConfig) : Listener {
    private val warping = mutableMapOf<UUID, Job>()

    fun isWarping(player: Player) = player.uniqueId in warping

    /**
     * @param departure where the player is placed before the screen fades, e.g. inside a gondola
     * @param cost taken just before the destination teleport, the ride is called off if the player can no longer pay
     */
    fun warp(player: Player, destination: WarpDestination, transition: WarpTransition, departure: Location? = null, cost: Cost = Cost.FREE): Boolean {
        if (isWarping(player) || player.isInCinematic()) return false
        val job = abyss.launch(start = CoroutineStart.LAZY) {
            try {
                ride(player, destination, transition, departure, cost)
            } finally {
                if (transition.title != null) player.clearTitle()
                player.stopLoop(transition)
                restore(player)
                warping.remove(player.uniqueId, coroutineContext.job)
            }
        }
        warping[player.uniqueId] = job
        job.start()
        return true
    }

    private suspend fun ride(player: Player, destination: WarpDestination, transition: WarpTransition, departure: Location?, cost: Cost) = coroutineScope {
        if (departure != null) {
            dialogSessions?.close(player)
            player.teleportAsync(departure).await()
        }
        // Held until the teleport has finished, then sent again to fade out from there
        transition.title?.let { player.holdFadeTitle(it, transition.fadeIn) }
        if (transition.freeze) freeze(player)
        transition.fadeEffects().takeIf { it.isNotEmpty() }?.let { effects ->
            player.persistentDataContainer.set(EFFECTS_KEY, PersistentDataType.LIST.strings(), effects.map { it.asString() })
        }

        val loop = transition.loopSound?.let { sound ->
            launch {
                do {
                    player.playTransitionSound(sound, transition)
                    delay(transition.loopSoundInterval)
                } while (transition.loopSoundInterval.isPositive())
            }
        }
        fade(player, transition, transition.fadeIn, darken = true)
        // A dialog that started the warp stays readable during the fade
        dialogSessions?.close(player)

        delay(transition.travelTime)
        loop?.cancel()
        player.stopLoop(transition)

        if (player.isDead) return@coroutineScope
        if (!cost.take(player)) return@coroutineScope player.error("You can no longer pay ${cost.describe()} for this trip")
        if (!player.teleportAsync(destination.randomLocation()).await()) {
            cost.refund(player)
            return@coroutineScope player.error("The trip was called off, nothing was charged")
        }
        transition.arriveSound?.let { player.playTransitionSound(it, transition) }
        transition.title?.let { player.releaseFadeTitle(it, transition.fadeOut) }
        fade(player, transition, transition.fadeOut, darken = false)
    }

    private fun Player.stopLoop(transition: WarpTransition) = transition.loopSound?.let { stopSound(SoundStop.named(it)) }

    private fun Player.playTransitionSound(sound: Key, transition: WarpTransition) =
        playSound(Sound.sound(sound, Sound.Source.AMBIENT, transition.volume, 1f))

    private suspend fun fade(player: Player, transition: WarpTransition, duration: Duration, darken: Boolean) {
        val steps = transition.fadeEffects().takeUnless { it.isEmpty() } ?: return delay(duration)
        player.slidePostEffects(steps, darken, duration) { delay(it) }
    }

    private fun freeze(player: Player) {
        player.freezeWalkSpeed()
        player.getAttribute(Attribute.JUMP_STRENGTH)?.run {
            removeModifier(FREEZE_KEY)
            addTransientModifier(AttributeModifier(FREEZE_KEY, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
        }
    }

    // Walk speed and post effects are saved with the player, the markers let a warp cut off by a crash be undone on join
    private fun restore(player: Player) {
        val data = player.persistentDataContainer
        data.get(EFFECTS_KEY, PersistentDataType.LIST.strings())?.let { effects ->
            player.showPostEffectStep(effects.mapNotNull { runCatching { Key.key(it) }.getOrNull() }, null)
            data.remove(EFFECTS_KEY)
        }
        player.restoreWalkSpeed()
        player.getAttribute(Attribute.JUMP_STRENGTH)?.removeModifier(FREEZE_KEY)
    }

    fun cancelAll() = warping.values.toList().forEach(Job::cancel)

    // The cancelled ride may only clean up once it is dispatched again, after the player has been saved
    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        warping.remove(player.uniqueId)?.cancel() ?: return
        restore(player)
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() = restore(player)
}
