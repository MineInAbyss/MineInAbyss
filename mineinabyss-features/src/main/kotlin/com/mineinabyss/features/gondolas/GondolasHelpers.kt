package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import com.mineinabyss.features.gondolas.pass.removeRoute
import org.bukkit.Location
import org.bukkit.entity.Player

enum class GondolaType {
    UPPER,
    LOWER,
    NONE;
}

object GondolasHelpers {
    fun gondolaWarp(gondola: Gondola, player: Player, gondolaType: GondolaType, gondolaId: String? = null) {
        player.teleportAsync(if (gondolaType == GondolaType.LOWER) gondola.upperLoc else gondola.lowerLoc)
        if (gondola.consumeTicket && (gondolaId != null))
            player.removeRoute(gondolaId)
    }

    fun locContains(loc: Location, point: Location, radius: Double): Boolean {
        if (loc.world.uid != point.world.uid) return false
        return loc.distanceSquared(point) <= radius * radius
    }

    fun closestGondolaData(gondola: Gondola, location: Location, id: String): GondolaData {
        if (locContains(gondola.upperLoc, location, gondola.warpZoneRange))
            return GondolaData(id, gondola,GondolaType.UPPER)

        if (locContains(gondola.lowerLoc, location, gondola.warpZoneRange))
            return GondolaData(id, gondola,GondolaType.LOWER)

        return GondolaData(id, gondola, GondolaType.NONE)
    }
}
