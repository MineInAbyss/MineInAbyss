package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola
import kotlinx.serialization.Serializable

@Serializable
data class GondolasConfig (
    val gondolas : Set<Gondola> = setOf()
) {
  init {
    // set the gondolas to be loaded at startup
    LoadedGondolas.loaded = gondolas.associateBy { it.name.lowercase() } as MutableMap<String, Gondola>
  }
}