package com.mineinabyss.features.layers

import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.event.HandlerList

val LayersFeature = feature("layers") {
    override val dependsOn = setOf("DeeperWorld")

    override fun FeatureDSL.enable() {
        plugin.listeners(context.layersListener)
    }

    override fun FeatureDSL.disable() {
        HandlerList.unregisterAll(context.layersListener)
    }
}
