package com.mineinabyss.components.cosmetics

import com.hibiscusmc.hmccosmetics.config.WardrobeLocation
import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
data class PersonalWardrobe(
    val viewerLocation: @Serializable(with = LocationSerializer::class) Location? = null,
    val npcLocation: @Serializable(with = LocationSerializer::class) Location? = null,
    val leaveLocation: @Serializable(with = LocationSerializer::class) Location? = null
) {
    val wardrobeLocation: WardrobeLocation
        get() = WardrobeLocation(npcLocation, viewerLocation, leaveLocation)
}
