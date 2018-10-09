package com.derongan.minecraft.mineinabyss.world;

import com.derongan.minecraft.deeperworld.world.section.Section;
import org.bukkit.World;

import java.util.List;

/**
 * Manages layers and sections
 */
public interface AbyssWorldManager {
    /**
     * Get all layers in index order.
     * @return All layers
     */
    List<Layer> getLayers();

    /**
     * Check if the world is a world set up for the abyss
     * @param worldName
     * @return
     */
    boolean isAbyssWorld(World worldName);

    Layer getLayerForSection(Section section);
}
