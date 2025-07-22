package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import kotlinx.serialization.Serializable

@Serializable
data class GondolasConfig(
    val gondolas: Set<Gondola> = setOf()
) {
    init {
        LoadedGondolas.loaded = gondolas.associateByTo(mutableMapOf()) { it.name.lowercase() }
    }
}