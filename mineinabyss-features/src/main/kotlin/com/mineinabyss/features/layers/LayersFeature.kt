package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.deeperworld.services.WorldManager
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("layers")
class LayersFeature(
    val layers: List<Layer>,
    private val hubSectionName: String = "orth",
) : AbyssFeature {
    val hubSection by lazy {
        WorldManager.getSectionFor(hubSectionName) ?: error("Section $hubSectionName was not found for the hub.")
    }

    @Transient
    val abyssWorldManager = AbyssWorldManagerImpl(layers)

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(LayerListener())
    }
}
