package com.mineinabyss.components.relics

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:sickle")
@AutoscanComponent
data class Sickle(
    val radius: Int = 3
)
