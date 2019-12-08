package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import org.bukkit.World

/**
 * Manages layers and sections
 */
interface AbyssWorldManager {
    /**
     * Get all layers in index order.
     * @return All layers
     */
    val layers: List<Layer?>?

    /**
     * Check if the world is a world set up for the abyss
     * @param worldName
     * @return
     */
    fun isAbyssWorld(worldName: World): Boolean

    fun getLayerForSection(section: Section): Layer
}