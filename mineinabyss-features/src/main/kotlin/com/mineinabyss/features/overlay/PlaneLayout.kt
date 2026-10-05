package com.mineinabyss.features.overlay

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.pow
import kotlin.math.tan

object PlaneLayout {
    const val PLANE_DISTANCE = 2.0
    const val NEAR_DISTANCE = 1.9
    const val PAGE_ASPECT = 2.0
    const val PAGE_SCALE = 0.75
    private const val DEFAULT_FOV = 70.0
    private val halfHeight = PLANE_DISTANCE * tan(Math.toRadians(DEFAULT_FOV / 2))

    val pageHeightBlocks = 2 * halfHeight * PAGE_SCALE
    val pageWidthBlocks = pageHeightBlocks * PAGE_ASPECT

    const val TEXT_PIXEL = 0.025

    fun worldAt(eye: Location, x: Double, y: Double, depth: Double = PLANE_DISTANCE): Location {
        val k = depth / PLANE_DISTANCE
        val forward = eye.direction
        val right = Vector(0, 1, 0).crossProduct(forward).normalize().multiply(-1)
        val up = right.clone().crossProduct(forward).normalize()
        return eye.clone()
            .add(forward.clone().multiply(depth))
            .add(right.multiply((x - 0.5) * pageWidthBlocks * k))
            .add(up.multiply((0.5 - y) * pageHeightBlocks * k))
    }

    fun textHeight(pixels: Number, scale: Float) = pixels.toDouble() * TEXT_PIXEL * scale / pageHeightBlocks
    fun textWidth(pixels: Number, scale: Float) = pixels.toDouble() * TEXT_PIXEL * scale / pageWidthBlocks
}

// In page units, x and y are the top left corner
data class Box(val x: Double, val y: Double, val w: Double, val h: Double) {
    fun contains(cx: Float, cy: Float) = cx >= x && cx <= x + w && cy >= y && cy <= y + h

    // Page x spans twice the distance of page y
    fun distanceSq(cx: Float, cy: Float) = ((cx - x - w / 2) * PlaneLayout.PAGE_ASPECT).pow(2) + (cy - y - h / 2).pow(2)
}

class PlaneView(val eye: Location, val fit: Double) {
    fun at(x: Double, y: Double, depth: Double = PlaneLayout.PLANE_DISTANCE) = PlaneLayout.worldAt(eye, x, y, depth * fit)
    fun scale(scale: Float) = (scale * fit).toFloat()
}
