package com.mineinabyss.features.cinematics

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.spawnStand
import com.mineinabyss.features.helpers.zeroCameraDistance
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity

internal suspend fun CinematicSession.camera(step: CameraStep) {
    step.follow?.let { id ->
        val mannequin = mannequins[id] ?: return abyss.logger.w("Cinematic $key follows unknown mannequin $id")
        return view(mannequin)
    }
    val positions = listOfNotNull(step.at) + step.path
    if (positions.isEmpty()) return abyss.logger.w("Cinematic $key has a camera step without a position")
    val path = ScenePath.of(anchor, positions)
    val target = lookTarget(step)
    val camera = cameraAt(aim(path.at(0.0), target))
    view(camera)
    if (positions.size == 1) return
    val ticks = step.duration.tickCount()
    var tick = 0
    while (tick < ticks && camera.isValid) {
        tick = if (skipping) ticks else tick + 1
        place(camera, aim(path.at(step.ease.apply(tick.toDouble() / ticks)), target))
        tick()
    }
}

private fun CinematicSession.lookTarget(step: CameraStep): (() -> Location?)? {
    step.lookAtPosition?.let { position -> return { anchor.at(position) } }
    val id = step.lookAt ?: return null
    return {
        riders[id]?.let { rider -> rider.footing.add(0.0, (rider.rider as? LivingEntity)?.eyeHeight ?: 0.0, 0.0) }
            ?: mannequins[id]?.eyeLocation
            ?: gondolas[id]?.location
    }
}

private fun aim(location: Location, target: (() -> Location?)?): Location =
    target?.invoke()?.let { ScenePath.facing(location, it) } ?: location

private fun CinematicSession.cameraAt(eye: Location): ArmorStand {
    camera?.takeIf { it.isValid }?.let { return it.also { place(it, eye) } }
    val stand = spawnStand(player, eye) { stand ->
        stand.isMarker = true
        stand.zeroCameraDistance()
    }
    place(stand, eye)
    camera = stand
    return stand
}

// Positions are where the eye goes, a stand's eye sits above its feet
private fun place(camera: ArmorStand, eye: Location) {
    camera.teleport(eye.clone().subtract(0.0, camera.eyeHeight, 0.0))
}
