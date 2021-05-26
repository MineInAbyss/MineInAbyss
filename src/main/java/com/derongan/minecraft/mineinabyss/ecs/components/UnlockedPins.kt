package com.derongan.minecraft.mineinabyss.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@AutoscanComponent
@SerialName("mineinabyss:unlocked_pins")
data class UnlockedPins(
    val pins: MutableSet<String>
)
