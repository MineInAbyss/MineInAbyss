package com.mineinabyss.features.cinematics

import org.bukkit.Location
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

/** Turns [ScenePosition]s into world locations, offsets turn with [origin]'s yaw so a scene can be played facing any way */
class CinematicAnchor(origin: Location) {
    val origin: Location = origin.clone().apply { pitch = 0f }
    private val radians = Math.toRadians(origin.yaw.toDouble())
    private val cos = cos(radians)
    private val sin = sin(radians)

    // Yaw 0 faces +z, so a local +z offset lands in front of the anchor
    fun at(position: ScenePosition): Location = origin.clone().add(
        position.x * cos - position.z * sin,
        position.y,
        position.x * sin + position.z * cos,
    ).apply {
        yaw = origin.yaw + (position.yaw ?: 0f)
        pitch = position.pitch ?: 0f
    }

    fun relative(location: Location): ScenePosition {
        val dx = location.x - origin.x
        val dz = location.z - origin.z
        return ScenePosition(
            (dx * cos + dz * sin).rounded(),
            (location.y - origin.y).rounded(),
            (-dx * sin + dz * cos).rounded(),
            Location.normalizeYaw(location.yaw - origin.yaw).rounded(),
            location.pitch.rounded(),
        )
    }

    private fun Double.rounded() = round(this * 100) / 100
    private fun Float.rounded() = round(this * 10) / 10

    companion object {
        /** Block center with the yaw snapped to a side, so the same spot gives the same anchor every time */
        fun snapped(location: Location) = CinematicAnchor(location.toBlockLocation().add(0.5, 0.0, 0.5).apply {
            yaw = round(location.yaw / 90f) * 90f
        })
    }
}
