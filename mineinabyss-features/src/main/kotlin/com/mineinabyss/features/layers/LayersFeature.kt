package com.mineinabyss.features.layers

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.event.HandlerList

//TODO change serialization mechanic
class LayersFeature : AbyssFeatureWithContext<LayersContext>(LayersContext::class) {
    override fun createContext() = LayersContext()

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(context.layersListener)
    }

    override fun MineInAbyssPlugin.disableFeature() {
        HandlerList.unregisterAll(context.layersListener)
    }
}
