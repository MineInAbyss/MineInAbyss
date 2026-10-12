package com.mineinabyss.features.cinematics

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.atan2
import kotlin.math.sqrt

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
