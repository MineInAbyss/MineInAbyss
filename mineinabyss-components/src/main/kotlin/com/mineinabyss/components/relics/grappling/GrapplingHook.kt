package com.mineinabyss.components.relics.grappling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:grappling_hook")
data class GrapplingHook(
    val range: Double,
    val hookSpeed: Double,
    val pullSpeed: Double,
    val pullStrength: Double,
    val type: GrapplingHookType,
)

enum class GrapplingHookType {
    MECHANICAL, MANUAL
}
