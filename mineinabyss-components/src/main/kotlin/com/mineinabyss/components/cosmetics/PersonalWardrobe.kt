package com.mineinabyss.components.cosmetics

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
data class PersonalWardrobe(
    val viewerLocation: @Serializable(with = LocationSerializer::class) Location? = null,
    val wardrobeLocation: @Serializable(with = LocationSerializer::class) Location? = null,
    val leaveLocation: @Serializable(with = LocationSerializer::class) Location? = null
)
