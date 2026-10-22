package com.mineinabyss.features.cinematics

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.spawnStand
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity

// Vanilla mount offsets, a mannequin hangs 0.6 below the point it mounts on, an armor stand carries riders at its full height
internal const val MANNEQUIN_VEHICLE_ATTACHMENT = 0.6
private const val ARMOR_STAND_HEIGHT = 1.975
// Long enough for ModelEngine to have sent the model after the client gets the carrier
internal const val LINK_TICKS = 5

/** A mannequin or the player standing on a gondola, riding [riser] which rides the gondola's carrier on the viewer's client */
internal class GondolaRider(val rider: Entity, val riser: ArmorStand, val gondola: CinematicGondola, val floor: Double) {
    /** Where [rider] stands, read from the gondola since a rider's server side position stays behind during the ride */
    val footing: Location
        get() = gondola.floorAt(floor).apply { yaw = rider.location.yaw; pitch = rider.location.pitch }
}

internal suspend fun CinematicSession.gondola(step: GondolaStep) {
    if (!abyss.isModelEngineEnabled) return abyss.logger.w("Cinematic $key needs ModelEngine for gondola ${step.id}")
    val gondola = gondolas[step.id] ?: run {
        val start = step.at ?: step.path.firstOrNull() ?: return abyss.logger.w("Cinematic $key spawns gondola ${step.id} without a position")
        val spawned = CinematicGondola.spawn(step.model, anchor.at(start), player)
            ?: throw SceneFailure("gondola ${step.id} uses unknown model ${step.model}")
        gondolas[step.id] = spawned
        // ModelEngine shows the model on its own update after the carrier reaches the client
        delay(LINK_TICKS.ticks)
        spawned.link()
        spawned
    }
    step.animation?.let(gondola::play)
    step.carry.forEach { id -> board(id, gondola, step.floor) }
    if (step.carryPlayer) boardPlayer(gondola, step.floor)

    if (step.fall) fall(gondola, step)
    else if (step.path.isNotEmpty()) travel(gondola, step)
}

/** Puts a mannequin in the middle of the gondola's floor and mounts it, it stays mounted until removed */
private suspend fun CinematicSession.board(id: String, gondola: CinematicGondola, floor: Double) {
    if (riders[id]?.gondola === gondola) return
    val mannequin = mannequins[id] ?: return abyss.logger.w("Cinematic $key carries unknown mannequin $id")
    val riser = spawnRiser(gondola, floor) ?: return
    mount(id, mannequin, riser, gondola, floor, mannequin.location.yaw)
    linkRiders(gondola)
}

/**
 * Moves the player from their seat onto the gondola, facing the way the view faces, then views through their own eyes.
 * Riding the model like this keeps them in step with it while they look around freely
 */
private suspend fun CinematicSession.boardPlayer(gondola: CinematicGondola, floor: Double) {
    if (carried?.gondola === gondola) return
    val looking = (viewing as? LivingEntity)?.location ?: player.location
    val riser = spawnRiser(gondola, floor) ?: return
    val oldSeat = seat
    relocating = true
    try {
        oldSeat.removePassenger(player)
        mount(PLAYER_RIDER, player, riser, gondola, floor, looking.yaw, looking.pitch)
    } finally {
        relocating = false
    }
    oldSeat.remove()
    seat = riser
    linkRiders(gondola)
    val followed = viewing
    view(player)
    mannequins.entries.firstOrNull { it.value === followed }?.key?.let(::remove)
    hideBody()
}

private fun CinematicSession.mount(id: String, entity: Entity, riser: ArmorStand, gondola: CinematicGondola, floor: Double, yaw: Float, pitch: Float = 0f) {
    entity.teleport(gondola.floorAt(floor).also { it.yaw = yaw; it.pitch = pitch })
    riser.addPassenger(entity)
    riders[id] = GondolaRider(entity, riser, gondola, floor)
}

private suspend fun linkRiders(gondola: CinematicGondola) {
    delay(TRACKER_TICKS.ticks)
    gondola.link()
}

// Mounted on the carrier, raised so a rider hanging the vanilla 0.6 below it stands on the floor
private fun CinematicSession.spawnRiser(gondola: CinematicGondola, floor: Double): ArmorStand? {
    val base = gondola.riderBase
    val riseTo = gondola.location.y + floor + MANNEQUIN_VEHICLE_ATTACHMENT - base.y
    if (riseTo <= 0) return null.also { abyss.logger.w("Gondola floor $floor is below where its model mounts riders, lower the model's eye height") }
    val riser = spawnStand(player, base) { it.getAttribute(Attribute.SCALE)?.baseValue = riseTo / ARMOR_STAND_HEIGHT }
    gondola.addRider(riser)
    return riser
}

private suspend fun CinematicSession.travel(gondola: CinematicGondola, step: GondolaStep) {
    val start = gondola.location
    val points = step.path.map(anchor::at)
    val rotations = step.path.zip(points) { position, point -> (if (position.yaw != null) point.yaw else start.yaw) to 0f }
    val path = ScenePath(listOf(start) + points, listOf(start.yaw to 0f) + rotations)
    val ticks = step.duration.tickCount()
    var tick = 0
    while (tick < ticks && !restored) {
        tick()
        tick = if (skipping) ticks else tick + 1
        gondola.moveTo(path.at(step.ease.apply(tick.toDouble() / ticks)).apply { pitch = 0f })
    }
}

// The model has no collision, so a rider is left on whatever blocks it landed on
internal fun landing(rider: GondolaRider): Location = ground(rider.footing, rider.floor + 1)
