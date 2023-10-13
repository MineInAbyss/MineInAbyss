package com.mineinabyss.features.layers

import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.Configurable
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("layers")
class LayersFeature : AbyssFeature, Configurable<LayersConfig> {
    @Transient
    override val configManager = config<LayersConfig>("layers", abyss.plugin.dataFolder.toPath(), LayersConfig())
    override val config by configManager

    @Transient
    val worldManager: AbyssWorldManager = SingleAbyssWorldManager(config.layers)

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(LayerListener())
    }
}
