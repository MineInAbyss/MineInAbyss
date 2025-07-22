package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import org.bukkit.entity.Player
import org.bukkit.Location
import kotlin.math.abs

enum class GondolaType() {
    UPPER,
    LOWER,
    NONE;
}

fun gondolaWarp(gondola: Gondola, player: Player, gondolaType: GondolaType) {
    val loc =
        if (gondolaType == GondolaType.LOWER) gondola.upperLoc else gondola.lowerLoc
    player.teleportAsync(loc)
    player.sendMessage("You have been warped to ${gondola.name} at ${loc.x}, ${loc.y}, ${loc.z}")
}

// AABB detection
// returns if locations contains point within radius
fun locContains(loc: Location, point: Location, radius: Double): Boolean {
    return abs(loc.x - point.x) <= radius &&
            abs(loc.y - point.y) <= radius &&
            abs(loc.z - point.z) <= radius
}

fun getClosestGondolaType(gondola: Gondola, location: Location): GondolaType {
    val radius = gondola.warpZoneRange
    val upperLoc = gondola.upperLoc
    val lowerLoc = gondola.lowerLoc

    // 0 for upperLoc, 1 for lowerLoc
    if (locContains(upperLoc, location, radius)) return GondolaType.UPPER
    if (locContains(lowerLoc, location, radius)) return GondolaType.LOWER
    return GondolaType.NONE
}

fun getGondolaFromName(gondolaName: String): Gondola? {
    return LoadedGondolas.loaded.values.firstOrNull {
        it.name.equals(
            gondolaName,
            ignoreCase = true
        )
    }
}