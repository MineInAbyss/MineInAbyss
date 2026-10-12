package com.mineinabyss.features.cinematics

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.dialogs.runtime.dialogContext
import com.mineinabyss.features.dialogs.runtime.runIn
import com.mineinabyss.features.helpers.freezeWalkSpeed
import com.mineinabyss.features.helpers.holdFadeTitle
import com.mineinabyss.features.helpers.lockCameraDistance
import com.mineinabyss.features.helpers.postEffectSteps
import com.mineinabyss.features.helpers.releaseCameraDistance
import com.mineinabyss.features.helpers.releaseFadeTitle
import com.mineinabyss.features.helpers.removePostEffects
import com.mineinabyss.features.helpers.restoreWalkSpeed
import com.mineinabyss.features.helpers.slidePostEffects
import com.mineinabyss.features.helpers.zeroCameraDistance
import com.mineinabyss.features.overlay.Overlays
import com.mineinabyss.features.warps.isWarping
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.nms.aliases.toNMS
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.datacomponent.item.ResolvableProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mannequin
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
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import java.util.*
import kotlin.time.Duration

private const val BAR_STEPS = 4
private val BARS = Key.key("mineinabyss", "cinematic/bars")
private val STALE_EFFECT_PREFIXES = listOf("cinematic/", "${DEFAULT_FADE.value()}_")
private const val MAX_TURN_PER_TICK = 10f
private const val MAX_PITCH_PER_TICK = 4f
// A step up or down, further than this the path's own height is kept, e.g. stepping off an edge onto a model
private const val GROUND_SEARCH = 0.6
// A new entity reaches the client on the tracker tick after it spawns, a camera packet or mount sent before that is ignored
internal const val TRACKER_TICKS = 3
private val COPIED_SLOTS = listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
private val CAMERA_KEY = NamespacedKey("mineinabyss", "cinematic/camera")
internal const val PLAYER_RIDER = "@player"
private const val SKIP_SETTLE_TICKS = 10
internal const val TICKS_PER_SECOND = 20.0

internal class SceneFailure(message: String) : Exception(message)

internal class CinematicSession(
    val player: Player,
    val key: PrefabKey,
    val scene: CinematicScene,
    val anchor: CinematicAnchor,
    val environment: Map<String, Any?>,
    val origin: Location,
    /** What the player rides, a seat where they started until they board a gondola */
    var seat: ArmorStand,
) {
    lateinit var job: Job
    var completed = false
    var restored = false
    var skipRequested = false
    /** Set once the screen is faded out, from then on the rest of the scene plays out without waiting */
    var skipping = false
    // Set while the player is moved from one seat to another, so the dismount is not read as a skip
    var relocating = false
    var camera: ArmorStand? = null
    var viewing: Entity? = null
    val mannequins = mutableMapOf<String, Mannequin>()
    val riders = mutableMapOf<String, GondolaRider>()
    val gondolas = mutableMapOf<String, CinematicGondola>()
    val effects = mutableSetOf<Key>()
    val chunkTickets = mutableSetOf<Chunk>()
    val carried: GondolaRider? get() = riders[PLAYER_RIDER]
}

class Cinematics internal constructor(private val registry: CinematicRegistry) : Listener {
    private val sessions = mutableMapOf<UUID, CinematicSession>()

    fun isPlaying(player: Player) = player.uniqueId in sessions

    fun play(player: Player, key: PrefabKey, anchor: CinematicAnchor, environment: Map<String, Any?> = emptyMap()): Boolean {
        val scene = registry[key] ?: return false.also { abyss.logger.w("Unknown cinematic $key") }
        if (isPlaying(player) || player.isWarping() || player.isInsideVehicle || player.isDead) return false
        dialogSessions?.close(player)

        val origin = player.location
        val seat = spawnStand(player, origin) { it.isMarker = true }
        val session = CinematicSession(player, key, scene, anchor, environment, origin, seat)
        sessions[player.uniqueId] = session
        seat.addPassenger(player)
        player.freezeWalkSpeed()
        // The hand is drawn in first person even while viewing through another entity
        Overlays.hideHands(player)
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
        if (ended && player.isOnline) session.scene.onEnd?.runIn(dialogContext(player, session.environment))
    }

    internal suspend fun tick(session: CinematicSession) {
        if (!session.skipping) delay(1.ticks)
    }

    private suspend fun pause(session: CinematicSession, duration: Duration) {
        var left = duration.tickCount()
        while (left-- > 0 && !session.skipping) delay(1.ticks)
    }

    private suspend fun CoroutineScope.run(session: CinematicSession) {
        for (step in session.scene.steps) {
            if (step.parallel) launch { perform(session, step) }
            else perform(session, step)
        }
    }

    private suspend fun perform(session: CinematicSession, step: CinematicStep) {
        when (step) {
            is WaitStep -> pause(session, step.duration)
            is BarsStep -> slide(session, BARS, BAR_STEPS, step.show, step.duration)
            is FadeStep -> slide(session, step.effect, step.effectSteps, step.darken, step.duration)
            is MannequinStep -> spawnMannequin(session, step)
            is WalkStep -> walk(session, step)
            is CameraStep -> camera(session, step)
            is GondolaStep -> gondola(session, step)
            is RemoveStep -> remove(session, step.id)
            is SoundStep -> if (!session.skipping) session.player.playSound(Sound.sound(Key.key(step.sound), Sound.Source.MASTER, step.volume, step.pitch))
            is ActionsStep -> step.actions.runIn(dialogContext(session.player, session.environment))
        }
    }

    /** Swaps through `<base>_1` to `<base>_<steps>`, showing them in order when [on], or backwards to none */
    private suspend fun slide(session: CinematicSession, base: Key, steps: Int, on: Boolean, duration: Duration) {
        val keys = postEffectSteps(base, steps.coerceAtLeast(1))
        session.effects += keys
        session.player.slidePostEffects(keys, on, duration) { pause(session, it) }
    }

    private fun spawnMannequin(session: CinematicSession, step: MannequinStep) {
        val player = session.player
        remove(session, step.id)
        val location = step.at?.let(session.anchor::at) ?: player.location
        val mannequin = player.world.spawn(location, Mannequin::class.java) { mannequin ->
            mannequin.isImmovable = true
            mannequin.setGravity(false)
            mannequin.isInvulnerable = true
            mannequin.isSilent = true
            mannequin.isPersistent = false
            mannequin.isVisibleByDefault = false
            mannequin.description = null
            mannequin.zeroCameraDistance()
            if (step.copyPlayer) {
                mannequin.profile = ResolvableProfile.resolvableProfile(player.playerProfile)
                mannequin.mainHand = player.mainHand
                COPIED_SLOTS.forEach { slot -> mannequin.equipment.setItem(slot, player.equipment.getItem(slot)) }
            }
        }
        player.showEntity(abyss, mannequin)
        session.mannequins[step.id] = mannequin
    }

    private suspend fun walk(session: CinematicSession, step: WalkStep) {
        val mannequin = session.mannequins[step.id] ?: return abyss.logger.w("Cinematic ${session.key} walks unknown mannequin ${step.id}")
        if (step.id in session.riders) return abyss.logger.w("Cinematic ${session.key} walks mannequin ${step.id} while it rides a gondola")
        val points = listOf(mannequin.location) + step.path.map(session.anchor::at)
        val lengths = points.zipWithNext { a, b -> a.distance(b) }
        val total = lengths.sum()
        val perTick = step.speed / TICKS_PER_SECOND
        var yaw = mannequin.location.yaw
        var pitch = mannequin.location.pitch
        var travelled = 0.0
        while (travelled < total && mannequin.isValid) {
            travelled = if (session.skipping) total else (travelled + perTick).coerceAtMost(total)
            var remaining = travelled
            var segment = 0
            while (segment < lengths.lastIndex && remaining > lengths[segment]) remaining -= lengths[segment++]
            val from = points[segment]
            val direction = points[segment + 1].toVector().subtract(from.toVector())
            val position = from.clone().add(direction.clone().multiply(if (lengths[segment] > 0) remaining / lengths[segment] else 1.0))
            if (direction.x != 0.0 || direction.z != 0.0) yaw = turn(yaw, position.clone().setDirection(direction).yaw)
            pitch -= pitch.coerceIn(-MAX_PITCH_PER_TICK, MAX_PITCH_PER_TICK)
            val next = (if (step.snapToGround) ground(position) else position).also { it.yaw = yaw; it.pitch = pitch }
            mannequin.teleport(next)
            mannequin.bodyYaw = yaw
            tick(session)
        }
        // A last point with a yaw is where the mannequin ends up looking
        if (step.path.lastOrNull()?.yaw != null) {
            val target = points.last().yaw
            while (mannequin.isValid && Location.normalizeYaw(target - yaw) != 0f) {
                yaw = if (session.skipping) target else turn(yaw, target)
                mannequin.setRotation(yaw, 0f)
                mannequin.bodyYaw = yaw
                tick(session)
            }
        }
    }

    private fun turn(from: Float, to: Float) = from + Location.normalizeYaw(to - from).coerceIn(-MAX_TURN_PER_TICK, MAX_TURN_PER_TICK)

    internal fun ground(location: Location, search: Double = GROUND_SEARCH): Location {
        val start = location.clone().add(0.0, search, 0.0)
        val hit = location.world.rayTraceBlocks(start, Vector(0, -1, 0), search * 2, FluidCollisionMode.NEVER, true)
        return hit?.let { location.clone().apply { y = it.hitPosition.y } } ?: location
    }

    private suspend fun camera(session: CinematicSession, step: CameraStep) {
        step.follow?.let { id ->
            val mannequin = session.mannequins[id] ?: return abyss.logger.w("Cinematic ${session.key} follows unknown mannequin $id")
            return view(session, mannequin)
        }
        val positions = listOfNotNull(step.at) + step.path
        if (positions.isEmpty()) return abyss.logger.w("Cinematic ${session.key} has a camera step without a position")
        val path = ScenePath.of(session.anchor, positions)
        val target = lookTarget(session, step)
        val camera = cameraAt(session, aim(path.at(0.0), target))
        view(session, camera)
        if (positions.size == 1) return
        val ticks = step.duration.tickCount()
        var tick = 0
        while (tick < ticks && camera.isValid) {
            tick = if (session.skipping) ticks else tick + 1
            place(camera, aim(path.at(step.ease.apply(tick.toDouble() / ticks)), target))
            tick(session)
        }
    }

    private fun lookTarget(session: CinematicSession, step: CameraStep): (() -> Location?)? {
        step.lookAtPosition?.let { position -> return { session.anchor.at(position) } }
        val id = step.lookAt ?: return null
        return {
            session.riders[id]?.let { rider -> rider.footing.add(0.0, (rider.rider as? LivingEntity)?.eyeHeight ?: 0.0, 0.0) }
                ?: session.mannequins[id]?.eyeLocation
                ?: session.gondolas[id]?.location
        }
    }

    private fun aim(location: Location, target: (() -> Location?)?): Location =
        target?.invoke()?.let { ScenePath.facing(location, it) } ?: location

    private fun cameraAt(session: CinematicSession, eye: Location): ArmorStand {
        session.camera?.takeIf { it.isValid }?.let { return it.also { place(it, eye) } }
        val camera = spawnStand(session.player, eye) { stand ->
            stand.isMarker = true
            stand.zeroCameraDistance()
        }
        place(camera, eye)
        session.camera = camera
        return camera
    }

    internal fun spawnStand(viewer: Player, at: Location, configure: (ArmorStand) -> Unit): ArmorStand =
        viewer.world.spawn(at, ArmorStand::class.java) { stand ->
            stand.isVisible = false
            stand.setGravity(false)
            stand.isInvulnerable = true
            stand.isPersistent = false
            stand.isVisibleByDefault = false
            configure(stand)
        }.also { viewer.showEntity(abyss, it) }

    // Positions are where the eye goes, a stand's eye sits above its feet
    private fun place(camera: ArmorStand, eye: Location) {
        camera.teleport(eye.clone().subtract(0.0, camera.eyeHeight, 0.0))
    }

    internal suspend fun view(session: CinematicSession, entity: Entity) {
        if (session.viewing === entity) return
        session.viewing = entity
        if (entity.ticksLived < TRACKER_TICKS) delay((TRACKER_TICKS - entity.ticksLived).ticks)
        if (session.viewing !== entity || session.restored) return
        session.player.toNMS().connection.send(ClientboundSetCameraPacket(entity.toNMS()))
        Overlays.hideHands(session.player)
    }

    internal fun remove(session: CinematicSession, id: String) {
        session.riders.remove(id)?.let { rider ->
            rider.gondola.removeRider(rider.riser)
            rider.riser.remove()
        }
        val mannequin = session.mannequins.remove(id)
        if (mannequin != null) mannequin.remove() else session.gondolas.remove(id)?.remove()
    }

    private fun restore(session: CinematicSession, returnPlayer: Boolean = true) {
        if (session.restored) return
        session.restored = true
        sessions.remove(session.player.uniqueId)
        val player = session.player
        val end = if (!session.completed) null
        else session.scene.endAt?.let { id -> session.riders[id]?.let { landing(it) } ?: session.mannequins[id]?.location }
            ?: session.carried?.let { landing(it) }
        if (player.isOnline) player.toNMS().run { connection.send(ClientboundSetCameraPacket(this)) }
        val effects = player.postEffects()
        session.effects.filter { it in effects.values() }.forEach(effects::remove)
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
        player.restoreWalkSpeed()
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

    // Post effects, walk speed and invisibility are saved with the player, so a scene cut off by a crash is undone on join
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.removePostEffects(STALE_EFFECT_PREFIXES)
        player.restoreWalkSpeed()
        showBody(player)
        sessions.values.filter { it.scene.hidePlayer }.forEach { player.hidePlayer(abyss, it.player) }
    }
}

internal fun Duration.tickCount() = inWholeTicks.toInt().coerceAtLeast(1)
