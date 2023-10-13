package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.services.WorldManager

class LayersConfig(
    val layers: List<Layer> = listOf(),
    private val hubSectionName: String = "orth",
) {
    val hubSection by lazy {
        WorldManager.getSectionFor(hubSectionName) ?: error("Section $hubSectionName was not found for the hub.")
    }
}
