package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import kotlinx.serialization.Serializable

@Serializable
data class GondolasConfig(
    val gondolas: Map<String, Gondola> = mapOf()
) {
    init {
        LoadedGondolas.loaded = gondolas.mapKeys { it.key.lowercase() }.toMutableMap()
    }
}