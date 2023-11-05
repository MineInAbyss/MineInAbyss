package com.mineinabyss.components.tools

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:sickle")
data class Sickle(
    val radius: Int = 3
)
