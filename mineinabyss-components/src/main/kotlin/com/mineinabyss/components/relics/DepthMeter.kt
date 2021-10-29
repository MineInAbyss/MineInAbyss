package com.mineinabyss.components.relics

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:depthmeter")
@AutoscanComponent
data class DepthMeter(
    val sectionXOffset: Int = 16384,
    val sectionYOffset: Int = 480,
    val abyssStartingHeightInOrth: Int = 0
)
