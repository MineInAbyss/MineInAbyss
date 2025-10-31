package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.features.gondolas.pass.removeRoute
import org.bukkit.entity.Player
import org.bukkit.Location

enum class GondolaType() {
    UPPER,
    LOWER,
    NONE;
}

fun gondolaWarp(gondola: Gondola, player: Player, gondolaType: GondolaType, gondolaId: String? = null) {
    val loc =
        if (gondolaType == GondolaType.LOWER) gondola.upperLoc else gondola.lowerLoc
    player.teleportAsync(loc)
    if (gondola.consumeTicket && (gondolaId != null)) {
        player.removeRoute(gondolaId)
    }
}

// returns if locations contains point within radius
fun locContains(loc: Location, point: Location, radius: Double): Boolean {
//    return abs(loc.x - point.x) <= radius &&
//            abs(loc.y - point.y) <= radius &&
//            abs(loc.z - point.z) <= radius
    return loc.distanceSquared(point) <= radius * radius
}

fun getClosestGondolaData(gondola: Gondola, location: Location, id: String): GondolaData {
    val radius = gondola.warpZoneRange
    val upperLoc = gondola.upperLoc
    val lowerLoc = gondola.lowerLoc

    if (locContains(upperLoc, location, radius))
        return GondolaData(id, gondola,GondolaType.UPPER)

    if (locContains(lowerLoc, location, radius))
        return GondolaData(id, gondola,GondolaType.LOWER)

    return GondolaData(id, gondola, GondolaType.NONE)
}
