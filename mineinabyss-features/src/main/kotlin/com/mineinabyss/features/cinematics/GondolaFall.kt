package com.mineinabyss.features.cinematics

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import net.kyori.adventure.sound.Sound
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

// Half the width of the gondola's underside, the corners of which are checked for ground when falling
private const val FALL_FOOTPRINT = 2.0
// Chunks around the gondola loaded in the next section before crossing, entities moved into unloaded chunks are discarded
private const val PRELOAD_RADIUS = 2
private const val CHUNK_WAIT_TICKS = 100
private const val RESPAWN_GAP_TICKS = 2
private const val SKIP_FALL_STEP = 8.0
private const val LAND_SOUND_PITCH = 0.6f
private const val RAY_EPSILON = 0.01

internal suspend fun CinematicSession.fall(gondola: CinematicGondola, step: GondolaStep) {
    val topSpeed = step.speed / TICKS_PER_SECOND
    val acceleration = step.acceleration / (TICKS_PER_SECOND * TICKS_PER_SECOND)
    var speed = 0.0
    var preload: Job? = null
    while (!restored) {
        tick()
        // Skipping drops in big steps, the client is behind a faded screen
        speed = if (skipping) SKIP_FALL_STEP else (speed + acceleration).coerceAtMost(topSpeed)
        val location = gondola.location
        val ground = dropUntilGround(location, speed)
        val next = location.subtract(0.0, ground ?: speed, 0.0)
        if (next.y < next.world.minHeight) throw SceneFailure("gondola ${step.id} fell out of the world without landing")
        gondola.moveTo(next)
        if (ground != null) {
            if (!skipping) step.landSound?.let { player.playSound(Sound.sound(it, Sound.Source.MASTER, 1f, LAND_SOUND_PITCH)) }
            return
        }
        if (preload == null) Sections.upcoming(next)?.let { upcoming ->
            preload = abyss.launch { loadAround(upcoming) }
        }
        val below = Sections.below(next) ?: continue
        preload?.join()
        preload = null
        crossSection(gondola, below, step)
    }
}

private suspend fun CinematicSession.loadAround(center: Location) {
    val world = center.world
    val chunkX = center.blockX shr 4
    val chunkZ = center.blockZ shr 4
    (-PRELOAD_RADIUS..PRELOAD_RADIUS).flatMap { dx -> (-PRELOAD_RADIUS..PRELOAD_RADIUS).map { dz -> chunkX + dx to chunkZ + dz } }
        .map { (x, z) -> world.getChunkAtAsync(x, z) }
        .forEach { future ->
            val chunk = future.await()
            if (restored) return
            if (chunk.addPluginChunkTicket(abyss)) chunkTickets += chunk
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

private fun CinematicSession.moveVehicle(vehicle: Entity, to: Location) {
    to.yaw = vehicle.location.yaw
    if (!vehicle.teleport(to)) abyss.logger.w("Cinematic $key could not move a gondola rider")
}

/**
 * Moves everything into the next DeeperWorld section at [to], the server side entities included.
 * The client drops entities that jump that far and adds them again, so the model, mounts and camera are sent again once it has them
 */
private fun CinematicSession.crossSection(gondola: CinematicGondola, to: Location, step: GondolaStep) {
    val delta = to.toVector().subtract(gondola.location.toVector())
    val onBoard = riders.values.filter { it.gondola === gondola }
    relocating = true
    try {
        gondola.jumpTo(to)
        onBoard.forEach { moveVehicle(it.riser, gondola.riderBase) }
        // The hidden player has to be in the new section for its chunks to reach them
        if (carried == null) moveVehicle(seat, to.clone().add(0.0, step.floor + MANNEQUIN_VEHICLE_ATTACHMENT, 0.0))
    } finally {
        relocating = false
    }
    mannequins.values.filter { mannequin -> onBoard.none { it.rider === mannequin } }.forEach { it.teleport(it.location.add(delta)) }
    camera?.let { it.teleport(it.location.add(delta)) }
    val viewed = viewing
    viewing = null
    // The fall carries on while the client catches up
    abyss.launch { resync(gondola, viewed) }
}

private suspend fun CinematicSession.resync(gondola: CinematicGondola, viewed: Entity?) {
    var waited = 0
    while (!restored && !player.isChunkSent(gondola.location.chunk) && waited++ < CHUNK_WAIT_TICKS) delay(1.ticks)
    if (restored) return
    gondola.hideFromViewer()
    delay(RESPAWN_GAP_TICKS.ticks)
    if (restored) return
    gondola.showToViewer()
    delay(LINK_TICKS.ticks)
    if (restored) return
    gondola.link()
    viewed?.let { view(it) }
}
