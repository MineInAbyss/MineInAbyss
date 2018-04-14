package com.derongan.minecraft.mineinabyss.World;

import com.google.common.collect.ImmutableList;
import org.bukkit.World;

import java.util.Collections;
import java.util.List;

public class LayerImpl implements Layer {
    private String layerName;
    private int index;
    private World world;

    private List<Section> sections;

    @Override
    public String getName() {
        return layerName;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getWorldName() {
        return world.getName();
    }

    @Override
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public Section getSection(int index) {
        return sections.get(index);
    }
}
