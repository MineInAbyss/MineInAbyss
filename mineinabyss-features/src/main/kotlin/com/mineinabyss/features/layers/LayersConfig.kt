package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.LayerConfig
import com.mineinabyss.deeperworld.deeperWorld
import kotlinx.serialization.Serializable

@Serializable
class LayersConfig(
    val layers: List<LayerConfig> = listOf(),
    private val hubSectionName: String = "orth",
) {
    val hubSection by lazy {
        deeperWorld.sections[hubSectionName] ?: error("Section $hubSectionName was not found for the hub.")
    }
}
