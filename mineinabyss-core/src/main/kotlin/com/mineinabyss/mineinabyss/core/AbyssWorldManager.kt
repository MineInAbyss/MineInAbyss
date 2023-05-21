package com.mineinabyss.mineinabyss.core

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.layer.LayerKey
import com.mineinabyss.deeperworld.world.section.Section
import org.bukkit.World

/**
 * Manages layers and sections
 *
 * @property layers [Layer]s associated by their [LayerKey]

 */
interface AbyssWorldManager {
    val layers: Map<LayerKey, Layer>

    /**
     * @param world
     * @return whether the world is a world set up for the abyss
     */
    fun isAbyssWorld(world: World): Boolean

    fun getLayerForSection(section: Section): Layer?

    fun getLayerFor(key: LayerKey): Layer?
}
