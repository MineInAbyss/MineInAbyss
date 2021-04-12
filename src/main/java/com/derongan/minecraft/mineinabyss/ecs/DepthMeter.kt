package com.derongan.minecraft.mineinabyss.ecs

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:depthmeter")
@AutoscanComponent
data class DepthMeter(
    val accuracy: Int = 1
)
