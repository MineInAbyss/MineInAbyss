package com.mineinabyss.components.tools

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:depthmeter")
data class DepthMeter(
    val sectionXOffset: Int = 16384,
    val sectionYOffset: Int = 480,
    val abyssStartingHeightInOrth: Int = 0
)
