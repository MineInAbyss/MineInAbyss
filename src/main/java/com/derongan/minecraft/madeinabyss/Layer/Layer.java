package com.derongan.minecraft.madeinabyss.Layer;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import com.derongan.minecraft.madeinabyss.Ascension.Effect.AscensionEffectBuilder;
import com.derongan.minecraft.madeinabyss.TickUtils;
import com.google.common.collect.ImmutableSet;

import java.util.*;

public class Layer {
    private String name;
    private Set<AscensionEffectBuilder> effectsOnLayer = new HashSet<>();
    private AbyssContext context;
    private String deathMessage;
    private int offset = 50;

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

    public void setEffectsOnLayer(Collection<Map> effects) {
        // TODO Improve this logic
        for (Map effect : effects) {
            int duration = (int) effect.getOrDefault("duration", 10);
            int strength = (int) effect.getOrDefault("strength", 1);
            int offset = (int) effect.getOrDefault("offset", 50); // TODO CURRENTLY IGNORED

            AscensionEffectBuilder builder = null;

            switch ((String) effect.get("name")) {
                case "BloodyAscensionEffect":
                    builder = new AscensionEffectBuilder.BloodyAscensionEffectBuilder();
                    break;
                case "DizzyAscensionEffect":
                    builder = new AscensionEffectBuilder.DizzyAscensionEffectBuilder();
                    break;
                case "DamagingAscensionEffect":
                    builder = new AscensionEffectBuilder.DamagingAscensionEffectBuilder();
                    break;
                case "HallucinatingAscensionEffect":
                    builder = new AscensionEffectBuilder.HallucinatingAscensionEffectBuilder();
                    break;
                case "SoundAscensionEffect":
                    builder = new AscensionEffectBuilder.SoundAscensionEffectBuilder();
                    ((AscensionEffectBuilder.SoundAscensionEffectBuilder) builder).setSounds((List<String>) effect.getOrDefault("sounds", null));
                    break;
                case "DeathAscensionEffect":
                    builder = new AscensionEffectBuilder.DeathAscensionEffectBuilder();
                    break;
            }

            if (builder != null) {
                builder.setContext(context)
                        .setDuration(TickUtils.milisecondsToTicks(duration*1000))
                        .setStrength(strength)
                        .setOffset(offset);

                effectsOnLayer.add(builder);
            }
        }
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
}
