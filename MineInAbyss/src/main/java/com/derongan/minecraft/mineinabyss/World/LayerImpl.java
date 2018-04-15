package com.derongan.minecraft.mineinabyss.World;

import com.google.common.collect.ImmutableList;
import org.bukkit.World;

import java.util.Collections;
import java.util.List;

public class LayerImpl implements Layer {
    private String layerName;
    private String layerSub;
    private int index;

    private List<Section> sections;

    public LayerImpl(String layerName, String layerSub, int index) {
        this.layerName = layerName;
        this.layerSub = layerSub;
        this.index = index;
    }

    @Override
    public String getName() {
        return layerName;
    }

    @Override
    public String getSub() {
        return layerSub;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public Section getSection(int index) {
        return sections.get(index);
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}
