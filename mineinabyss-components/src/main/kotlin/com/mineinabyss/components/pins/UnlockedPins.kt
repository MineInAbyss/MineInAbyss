package com.mineinabyss.components.pins

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:unlocked_pins")
data class UnlockedPins(
    val pins: MutableSet<String>
)
