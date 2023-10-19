package com.mineinabyss.features.layers

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeatureWithContext
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import org.bukkit.event.HandlerList

class LayersFeature : AbyssFeatureWithContext<LayersContext>(LayersContext::class) {
    override fun createContext() = LayersContext()

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(context.layersListener)
    }

    override fun MineInAbyssPlugin.disableFeature() {
        HandlerList.unregisterAll(context.layersListener)
    }
}
