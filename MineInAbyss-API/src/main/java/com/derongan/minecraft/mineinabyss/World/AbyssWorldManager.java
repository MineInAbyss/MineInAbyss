package com.derongan.minecraft.mineinabyss.World;

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
     * Get all sections in index order
     * @return All sections
     */
    List<Section> getSections();

    /**
     * Get the section at index or null if out of bounds
     * @param index the index
     * @return the section to get
     */
    Section getSectonAt(int index);

    /**
     * Get the layer at index or null if out of bounds
     * @param index the index
     * @return the layer to get
     */
    Layer getLayerAt(int index);

    /**
     * Check if the world is a world set up for the abyss
     * @param worldName
     * @return
     */
    boolean isAbyssWorld(String worldName);
}
