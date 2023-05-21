package com.mineinabyss.layers

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("layers")
class LayersFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(LayerListener())
    }
}
