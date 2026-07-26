package com.mineinabyss.features.layers

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerConfig
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.sections.SectionKey
import org.bukkit.World

/**
 * Manages layers and sections
 *
 * @property layers [LayerConfig]s associated by their [LayerKey]

 */
interface AbyssWorldManager {
    val layers: Map<LayerKey, Layer>

    /**
     * @param world
     * @return whether the world is a world set up for the abyss
     */
    fun isAbyssWorld(world: World): Boolean

    fun getLayerForSection(key: SectionKey): Layer?

    fun getLayerFor(key: LayerKey): Layer?
}
