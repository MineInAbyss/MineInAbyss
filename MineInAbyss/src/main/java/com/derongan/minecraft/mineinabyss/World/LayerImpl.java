package com.derongan.minecraft.mineinabyss.World;

import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffect;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffectBuilder;
import com.google.common.collect.ImmutableList;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayerImpl implements Layer {
    private String layerName;
    private String layerSub;
    private int index;

    private List<Section> sections;
    private List<AscensionEffectBuilder> effects;

    public LayerImpl(String layerName, String layerSub, int index) {
        this.layerName = layerName;
        this.layerSub = layerSub;
        this.index = index;

        this.effects = new ArrayList<>();
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

    public void setEffects(List<AscensionEffectBuilder> effects){
        this.effects = effects;
    }

    @Override
    public List<AscensionEffectBuilder> getAscensionEffects() {
        return Collections.unmodifiableList(effects);
    }
}
