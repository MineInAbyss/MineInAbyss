package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.services.WorldManager
import kotlinx.serialization.Serializable

@Serializable
class LayersConfig(
    val layers: Map<LayerKey, Layer> = mapOf(),
    private val hubSectionName: String = "orth",
) {
    val hubSection by lazy {
        WorldManager.getSectionFor(hubSectionName) ?: error("Section $hubSectionName was not found for the hub.")
    }
}
