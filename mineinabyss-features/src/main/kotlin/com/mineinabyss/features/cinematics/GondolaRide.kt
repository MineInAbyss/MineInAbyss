package com.mineinabyss.features.cinematics

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.sendTransparentSlots
import com.mineinabyss.features.overlay.Overlays
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import kotlin.time.Duration.Companion.seconds

// Vanilla mount offsets, a mannequin hangs 0.6 below the point it mounts on, an armor stand carries riders at its full height
private const val MANNEQUIN_VEHICLE_ATTACHMENT = 0.6
private const val ARMOR_STAND_HEIGHT = 1.975
// Half the width of the gondola's underside, the corners of which are checked for ground when falling
private const val FALL_FOOTPRINT = 2.0
// Chunks around the gondola loaded in the next section before crossing, entities moved into unloaded chunks are discarded
private const val PRELOAD_RADIUS = 2
private const val CHUNK_WAIT_TICKS = 100
// Long enough for ModelEngine to have sent the model after the client gets the carrier
private const val LINK_TICKS = 5
private const val RESPAWN_GAP_TICKS = 2
private val HIDDEN_KEY = NamespacedKey("mineinabyss", "cinematic/hidden")
private val BODY_REHIDE = 1.seconds
// Armor slots of the player's own inventory menu, head to feet
private val ARMOR_SLOTS = 5..8
private const val SKIP_FALL_STEP = 8.0
private const val LAND_SOUND_PITCH = 0.6f
private const val RAY_EPSILON = 0.01
private val BODY_INVISIBILITY = PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, false)

/** A mannequin or the player standing on a gondola, riding [riser] which rides the gondola's carrier on the viewer's client */
internal class GondolaRider(val rider: Entity, val riser: ArmorStand, val gondola: CinematicGondola, val floor: Double) {
    /** Where [rider] stands, read from the gondola since a rider's server side position stays behind during the ride */
    val footing: Location
        get() = gondola.location.add(0.0, floor, 0.0).apply { yaw = rider.location.yaw; pitch = rider.location.pitch }
}

internal suspend fun Cinematics.gondola(session: CinematicSession, step: GondolaStep) {
    if (!abyss.isModelEngineEnabled) return abyss.logger.w("Cinematic ${session.key} needs ModelEngine for gondola ${step.id}")
    val gondola = session.gondolas[step.id] ?: run {
        val start = step.at ?: step.path.firstOrNull() ?: return abyss.logger.w("Cinematic ${session.key} spawns gondola ${step.id} without a position")
        val spawned = CinematicGondola.spawn(step.model, session.anchor.at(start), session.player)
            ?: throw SceneFailure("gondola ${step.id} uses unknown model ${step.model}")
        session.gondolas[step.id] = spawned
        // ModelEngine shows the model on its own update after the carrier reaches the client
        delay(LINK_TICKS.ticks)
        spawned.link()
        spawned
    }
    step.animation?.let(gondola::play)
    step.carry.forEach { id -> board(session, id, gondola, step.floor) }
    if (step.carryPlayer) boardPlayer(session, gondola, step.floor)

    if (step.fall) fall(session, gondola, step)
    else if (step.path.isNotEmpty()) travel(session, gondola, step)
}

/** Puts a mannequin in the middle of the gondola's floor and mounts it, it stays mounted until removed */
private suspend fun Cinematics.board(session: CinematicSession, id: String, gondola: CinematicGondola, floor: Double) {
    if (session.riders[id]?.gondola === gondola) return
    val mannequin = session.mannequins[id] ?: return abyss.logger.w("Cinematic ${session.key} carries unknown mannequin $id")
    val riser = spawnRiser(session, gondola, floor) ?: return
    mannequin.teleport(gondola.location.add(0.0, floor, 0.0).apply { yaw = mannequin.location.yaw })
    riser.addPassenger(mannequin)
    session.riders[id] = GondolaRider(mannequin, riser, gondola, floor)
    delay(TRACKER_TICKS.ticks)
    gondola.link()
}

/**
 * Moves the player from their seat onto the gondola, facing the way the view faces, then views through their own eyes.
 * Riding the model like this keeps them in step with it while they look around freely
 */
private suspend fun Cinematics.boardPlayer(session: CinematicSession, gondola: CinematicGondola, floor: Double) {
    if (session.carried?.gondola === gondola) return
    val player = session.player
    val looking = (session.viewing as? LivingEntity)?.location ?: player.location
    val riser = spawnRiser(session, gondola, floor) ?: return
    val oldSeat = session.seat
    session.relocating = true
    try {
        oldSeat.removePassenger(player)
        player.teleport(gondola.location.add(0.0, floor, 0.0).apply { yaw = looking.yaw; pitch = looking.pitch })
        riser.addPassenger(player)
    } finally {
        session.relocating = false
    }
    oldSeat.remove()
    session.seat = riser
    session.riders[PLAYER_RIDER] = GondolaRider(player, riser, gondola, floor)
    delay(TRACKER_TICKS.ticks)
    gondola.link()
    val followed = session.viewing
    view(session, player)
    session.mannequins.entries.firstOrNull { it.value === followed }?.key?.let { remove(session, it) }
    hideBody(session)
}

/**
 * Riders are always drawn sitting, which the player sees of themselves in F5 even at zero distance.
 * Invisibility hides the body but not armor or held items, so those get transparent items on the player's own client, sent again in case an inventory update brings them back
 */
private fun hideBody(session: CinematicSession) {
    val player = session.player
    if (player.persistentDataContainer.has(HIDDEN_KEY)) return
    player.persistentDataContainer.set(HIDDEN_KEY, PersistentDataType.BOOLEAN, true)
    abyss.launch {
        while (!session.restored && player.isOnline) {
            // An invisibility potion the player already has is left to run out, then replaced by ours
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) player.addPotionEffect(BODY_INVISIBILITY)
            Overlays.hideHands(player)
            player.sendTransparentSlots(ARMOR_SLOTS)
            delay(BODY_REHIDE)
        }
    }
}

internal fun showBody(player: Player) {
    if (!player.persistentDataContainer.has(HIDDEN_KEY)) return
    player.persistentDataContainer.remove(HIDDEN_KEY)
    if (player.getPotionEffect(PotionEffectType.INVISIBILITY) == BODY_INVISIBILITY) player.removePotionEffect(PotionEffectType.INVISIBILITY)
}

// Mounted on the carrier, raised so a rider hanging the vanilla 0.6 below it stands on the floor
private fun Cinematics.spawnRiser(session: CinematicSession, gondola: CinematicGondola, floor: Double): ArmorStand? {
    val base = gondola.riderBase
    val riseTo = gondola.location.y + floor + MANNEQUIN_VEHICLE_ATTACHMENT - base.y
    if (riseTo <= 0) return null.also { abyss.logger.w("Gondola floor $floor is below where its model mounts riders, lower the model's eye height") }
    val riser = spawnStand(session.player, base) { it.getAttribute(Attribute.SCALE)?.baseValue = riseTo / ARMOR_STAND_HEIGHT }
    gondola.addRider(riser)
    return riser
}

private suspend fun Cinematics.travel(session: CinematicSession, gondola: CinematicGondola, step: GondolaStep) {
    val start = gondola.location
    val points = step.path.map(session.anchor::at)
    val rotations = step.path.zip(points) { position, point -> (if (position.yaw != null) point.yaw else start.yaw) to 0f }
    val path = ScenePath(listOf(start) + points, listOf(start.yaw to 0f) + rotations)
    val ticks = step.duration.tickCount()
    var tick = 0
    while (tick < ticks && !session.restored) {
        tick(session)
        tick = if (session.skipping) ticks else tick + 1
        gondola.moveTo(path.at(step.ease.apply(tick.toDouble() / ticks)).apply { pitch = 0f })
    }
}

private suspend fun Cinematics.fall(session: CinematicSession, gondola: CinematicGondola, step: GondolaStep) {
    val topSpeed = step.speed / TICKS_PER_SECOND
    val acceleration = step.acceleration / (TICKS_PER_SECOND * TICKS_PER_SECOND)
    var speed = 0.0
    var preload: Job? = null
    while (!session.restored) {
        tick(session)
        // Skipping drops in big steps, the client is behind a faded screen
        speed = if (session.skipping) SKIP_FALL_STEP else (speed + acceleration).coerceAtMost(topSpeed)
        val location = gondola.location
        val ground = dropUntilGround(location, speed)
        val next = location.subtract(0.0, ground ?: speed, 0.0)
        if (next.y < next.world.minHeight) throw SceneFailure("gondola ${step.id} fell out of the world without landing")
        gondola.moveTo(next)
        if (ground != null) {
            if (!session.skipping) step.landSound?.let { session.player.playSound(Sound.sound(Key.key(it), Sound.Source.MASTER, 1f, LAND_SOUND_PITCH)) }
            return
        }
        if (preload == null) Sections.upcoming(next)?.let { upcoming ->
            preload = abyss.launch { loadAround(session, upcoming) }
        }
        val below = Sections.below(next) ?: continue
        preload?.join()
        preload = null
        crossSection(session, gondola, below, step)
    }
}

private suspend fun loadAround(session: CinematicSession, center: Location) {
    val world = center.world
    val chunkX = center.blockX shr 4
    val chunkZ = center.blockZ shr 4
    (-PRELOAD_RADIUS..PRELOAD_RADIUS).flatMap { dx -> (-PRELOAD_RADIUS..PRELOAD_RADIUS).map { dz -> chunkX + dx to chunkZ + dz } }
        .map { (x, z) -> world.getChunkAtAsync(x, z) }
        .forEach { future ->
            val chunk = future.await()
            if (session.restored) return
            if (chunk.addPluginChunkTicket(abyss)) session.chunkTickets += chunk
        }
}

/** How far the gondola's underside is above a block it would reach this tick, null when nothing is within [speed] */
private fun dropUntilGround(location: Location, speed: Double): Double? {
    val corners = listOf(-FALL_FOOTPRINT, 0.0, FALL_FOOTPRINT)
    return corners.flatMap { dx -> corners.map { dz -> dx to dz } }.mapNotNull { (dx, dz) ->
        val start = location.clone().add(dx, RAY_EPSILON, dz)
        location.world.rayTraceBlocks(start, Vector(0, -1, 0), speed + RAY_EPSILON, FluidCollisionMode.NEVER, true)
            ?.let { (start.y - it.hitPosition.y - RAY_EPSILON).coerceAtLeast(0.0) }
    }.minOrNull()
}

private fun moveVehicle(session: CinematicSession, vehicle: Entity, to: Location) {
    to.yaw = vehicle.location.yaw
    if (!vehicle.teleport(to)) abyss.logger.w("Cinematic ${session.key} could not move a gondola rider")
}

/**
 * Moves everything into the next DeeperWorld section at [to], the server side entities included.
 * The client drops entities that jump that far and adds them again, so the model, mounts and camera are sent again once it has them
 */
private suspend fun Cinematics.crossSection(session: CinematicSession, gondola: CinematicGondola, to: Location, step: GondolaStep) {
    val delta = to.toVector().subtract(gondola.location.toVector())
    val riders = session.riders.values.filter { it.gondola === gondola }
    session.relocating = true
    try {
        gondola.jumpTo(to)
        riders.forEach { moveVehicle(session, it.riser, gondola.riderBase) }
        // The hidden player has to be in the new section for its chunks to reach them
        if (session.carried == null) moveVehicle(session, session.seat, to.clone().add(0.0, step.floor + MANNEQUIN_VEHICLE_ATTACHMENT, 0.0))
    } finally {
        session.relocating = false
    }
    session.mannequins.values.filter { mannequin -> riders.none { it.rider === mannequin } }.forEach { it.teleport(it.location.add(delta)) }
    session.camera?.let { it.teleport(it.location.add(delta)) }
    val viewing = session.viewing
    session.viewing = null
    // The fall carries on while the client catches up
    abyss.launch { resync(session, gondola, viewing) }
}

private suspend fun Cinematics.resync(session: CinematicSession, gondola: CinematicGondola, viewing: Entity?) {
    val player = session.player
    var waited = 0
    while (!session.restored && !player.isChunkSent(gondola.location.chunk) && waited++ < CHUNK_WAIT_TICKS) delay(1.ticks)
    if (session.restored) return
    gondola.hideFromViewer()
    delay(RESPAWN_GAP_TICKS.ticks)
    if (session.restored) return
    gondola.showToViewer()
    delay(LINK_TICKS.ticks)
    if (session.restored) return
    gondola.link()
    viewing?.let { view(session, it) }
}

// The model has no collision, so a rider is left on whatever blocks it landed on
internal fun Cinematics.landing(rider: GondolaRider): Location = ground(rider.footing, rider.floor + 1)
