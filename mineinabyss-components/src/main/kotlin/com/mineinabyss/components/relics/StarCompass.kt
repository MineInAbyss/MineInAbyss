package com.mineinabyss.components.relics

import com.mineinabyss.idofront.serialization.LocationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("mineinabyss:starcompass")
class StarCompass(
    var compassLocation: @Serializable(with = LocationSerializer::class) Location? = null
)
