package com.mineinabyss.components.relics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound

@Serializable
@SerialName("mineinabyss:bounding_lance")
class BoundingLance(
    val placeSound: Sound = Sound.BLOCK_CHAIN_PLACE
)