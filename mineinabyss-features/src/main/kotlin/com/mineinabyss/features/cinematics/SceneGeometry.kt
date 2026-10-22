package com.mineinabyss.features.cinematics

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Written as `[x, y, z]` or `[x, y, z, yaw, pitch]` relative to the scene's anchor.
 * The offset turns with the anchor's yaw, so +z is in front of the anchor
 */
@Serializable(with = ScenePosition.Serializer::class)
data class ScenePosition(val x: Double, val y: Double, val z: Double, val yaw: Float? = null, val pitch: Float? = null) {
    object Serializer : KSerializer<ScenePosition> {
        private val list = ListSerializer(Double.serializer())
        override val descriptor = list.descriptor

        override fun deserialize(decoder: Decoder): ScenePosition {
            val values = list.deserialize(decoder)
            require(values.size == 3 || values.size == 5) { "A position needs [x, y, z] or [x, y, z, yaw, pitch], got $values" }
            return ScenePosition(values[0], values[1], values[2], values.getOrNull(3)?.toFloat(), values.getOrNull(4)?.toFloat())
        }

        override fun serialize(encoder: Encoder, value: ScenePosition) =
            list.serialize(encoder, listOfNotNull(value.x, value.y, value.z, value.yaw?.toDouble(), value.pitch?.toDouble()))
    }
}

enum class Ease {
    LINEAR, IN, OUT, IN_OUT;

    fun apply(t: Double): Double = when (this) {
        LINEAR -> t
        IN -> t * t
        OUT -> 1 - (1 - t) * (1 - t)
        IN_OUT -> t * t * (3 - 2 * t)
    }
}

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

/** A smooth curve through [points], [rotations] holds the yaw and pitch written for each point, null where none was */
internal class ScenePath(private val points: List<Location>, private val rotations: List<Pair<Float, Float>?>) {
    init {
        require(points.isNotEmpty()) { "A path needs at least one point" }
    }

    private val lengths = points.zipWithNext { a, b -> a.distance(b) }

    // A point repeated to turn the view in place needs time of its own, so such a path is split evenly per point
    private val distances = lengths.takeIf { it.none { length -> length < 1e-6 } }?.runningFold(0.0, Double::plus)

    /** [t] runs from 0 at the first point to 1 at the last, shared out between points by distance */
    fun at(t: Double): Location {
        if (points.size == 1) return points.first().clone()
        val (segment, u) = segmentAt(t.coerceIn(0.0, 1.0))
        val p0 = points[(segment - 1).coerceAtLeast(0)].toVector()
        val p1 = points[segment].toVector()
        val p2 = points[segment + 1].toVector()
        val p3 = points[(segment + 2).coerceAtMost(points.size - 1)].toVector()
        val position = catmullRom(p0, p1, p2, p3, u)
        val location = position.toLocation(points.first().world)
        val from = rotations[segment]
        val to = rotations[segment + 1]
        if (from != null && to != null) {
            location.yaw = lerpYaw(from.first, to.first, u)
            location.pitch = (from.second + (to.second - from.second) * u).toFloat()
        } else {
            val ahead = catmullRom(p0, p1, p2, p3, (u + 0.01).coerceAtMost(1.0)).subtract(position)
            location.direction = if (ahead.lengthSquared() > 1e-6) ahead else p2.clone().subtract(p1)
        }
        return location
    }

    private fun segmentAt(t: Double): Pair<Int, Double> {
        if (distances == null) {
            val scaled = t * (points.size - 1)
            val segment = scaled.toInt().coerceAtMost(points.size - 2)
            return segment to scaled - segment
        }
        val travelled = t * distances.last()
        val segment = distances.indexOfLast { it <= travelled }.coerceIn(0, points.size - 2)
        return segment to ((travelled - distances[segment]) / lengths[segment]).coerceIn(0.0, 1.0)
    }

    private fun catmullRom(p0: Vector, p1: Vector, p2: Vector, p3: Vector, u: Double): Vector {
        val u2 = u * u
        val u3 = u2 * u
        fun axis(a: Double, b: Double, c: Double, d: Double) =
            0.5 * (2 * b + (-a + c) * u + (2 * a - 5 * b + 4 * c - d) * u2 + (-a + 3 * b - 3 * c + d) * u3)
        return Vector(axis(p0.x, p1.x, p2.x, p3.x), axis(p0.y, p1.y, p2.y, p3.y), axis(p0.z, p1.z, p2.z, p3.z))
    }

    companion object {
        fun of(anchor: CinematicAnchor, positions: List<ScenePosition>) = ScenePath(
            positions.map(anchor::at),
            positions.map { position -> anchor.at(position).takeIf { position.yaw != null }?.let { it.yaw to it.pitch } },
        )

        private fun lerpYaw(from: Float, to: Float, u: Double): Float = (from + Location.normalizeYaw(to - from) * u).toFloat()

        fun facing(from: Location, target: Location): Location {
            val delta = target.toVector().subtract(from.toVector())
            return from.clone().apply {
                yaw = Math.toDegrees(atan2(-delta.x, delta.z)).toFloat()
                pitch = Math.toDegrees(atan2(-delta.y, sqrt(delta.x * delta.x + delta.z * delta.z))).toFloat()
            }
        }
    }
}
