package com.mineinabyss.features.quests

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Location

// List of all the locations that are to be detected by quests

// For now, each location is a square of radius blocks centered around locationCenter
@Serializable
class LocationData(
    val name: String,
    val locationCenter: @Serializable(with = LocationAltSerializer::class) Location,
    val radius: Int,
) {
    //    var visited : Boolean = false
    @Transient val xRange = (locationCenter.blockX - radius)..(locationCenter.blockX + radius)
    @Transient val yRange = (locationCenter.blockY - radius)..(locationCenter.blockY + radius)
    @Transient val zRange = (locationCenter.blockZ - radius)..(locationCenter.blockZ + radius)

    fun isInside(location: Location, flat: Boolean = false): Boolean {
        return location.blockX in xRange && location.blockZ in zRange && (flat || location.blockY in yRange)
    }

}

