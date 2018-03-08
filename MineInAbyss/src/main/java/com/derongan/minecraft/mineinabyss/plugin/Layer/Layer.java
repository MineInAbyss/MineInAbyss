package com.derongan.minecraft.mineinabyss.plugin.Layer;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import com.derongan.minecraft.mineinabyss.plugin.Ascension.Effect.AscensionEffectBuilder;
import com.derongan.minecraft.mineinabyss.plugin.TickUtils;
import com.google.common.collect.ImmutableSet;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class Layer {
    private String name;
    private Set<AscensionEffectBuilder> effectsOnLayer;
    private List<Section> sections;
    private AbyssContext context;
    private String deathMessage;
    private int offset = 50;

    private Layer nextLayer;
    private Layer prevLayer;

    public Layer getNextLayer() {
        return nextLayer;
    }

    public void setNextLayer(Layer nextLayer) {
        this.nextLayer = nextLayer;
    }

    public Layer getPrevLayer() {
        return prevLayer;
    }

    public void setPrevLayer(Layer prevLayer) {
        this.prevLayer = prevLayer;
    }

    public Layer(String name, AbyssContext context) {
        this.name = name;
        this.context = context;
    }

    public String getName() {
        return name;
    }

    public ImmutableSet<AscensionEffectBuilder> getEffectsOnLayer() {
        return ImmutableSet.copyOf(effectsOnLayer);
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void setEffectsOnLayer(Set<AscensionEffectBuilder> effects) {
        effectsOnLayer = effects;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isLastSection(int i) {
        return i == sections.size() - 1;
    }

    public boolean isFirstSection(int i) {
        return i == 0;
    }

    public List<Section> getSections() {
        return sections;
    }

    public Section getSectionAbove(int i) {
        if (i != 0)
            return sections.get(i - 1);
        return null;
    }

    public Section getSectionBelow(int i) {
        if (i != sections.size() - 1)
            return sections.get(i + 1);
        return null;
    }
}
