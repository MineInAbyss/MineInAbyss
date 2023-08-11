package com.mineinabyss.components.relics.grappling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:grappling_hook")
data class GrapplingHook(
    val range: Double = 10.0,
    val hookSpeed: Double,
    val pullSpeed: Double,
)
