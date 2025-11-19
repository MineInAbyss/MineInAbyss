package com.mineinabyss.features.quests

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Location

// List of all the locations that are to be detected by quests

// For now, each location is a square of radius blocks centered around locationCenter
@Serializable
class LocationData(
    val name: String,
    val locationCenter: @Serializable(with = LocationSerializer::class) Location,
    val radius: Int,
) {
    //    var visited : Boolean = false
    val minX = locationCenter.blockX - radius
    val maxX = locationCenter.blockX + radius
    val minY = locationCenter.blockY - radius
    val maxY = locationCenter.blockY + radius
    val minZ = locationCenter.blockZ - radius
    val maxZ = locationCenter.blockZ + radius

    fun isInside(location: Location, flat: Boolean = false): Boolean {
        return location.blockX in minX..maxX && location.blockZ in minZ..maxZ && (flat || location.blockY in minY..maxY)
    }

}

@Serializable
class LocationsConfig(
    val locations: Map<String, LocationData> = emptyMap(),

    ) {
}

