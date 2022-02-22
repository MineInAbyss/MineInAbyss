package com.mineinabyss.components.relics

import com.mineinabyss.components.guilds.Whistle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:unheard_bell")
class UnheardBell (
    val whistleRequirement: Whistle = Whistle.WHITE,
    val effectRange: Double = 500.0,
    val effectDuration: Int = 500,
)

