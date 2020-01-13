package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.section.Section
import org.bukkit.World

/**
 * Manages layers and sections
 *
 * @property layers All layers in index order.

 */
interface AbyssWorldManager {
    val layers: List<Layer>

    /**
     * @param worldName
     * @return whether the world is a world set up for the abyss
     */
    fun isAbyssWorld(worldName: World): Boolean

    fun getLayerForSection(section: Section): Layer
}