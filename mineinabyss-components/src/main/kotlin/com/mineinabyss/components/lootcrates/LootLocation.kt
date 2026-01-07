package com.mineinabyss.components.lootcrates

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:loot_location")
class LootLocation(
    @Serializable(with = LocationAltSerializer::class)
    val location: Location
)
