package com.derongan.minecraft.mineinabyss.Ascension.Effect.Configuration;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffectBuilderImpl;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class EffectConfiguror {
    private FileConfiguration configuration;
    private AbyssContext context;

    public EffectConfiguror(FileConfiguration configuration, AbyssContext context) {
        this.configuration = configuration;
        this.context = context;
    }

    public Set<AscensionEffectBuilderImpl> getEffectsFromMap(Collection<Map> effects) {
        Set<AscensionEffectBuilderImpl> ascensionEffectBuilders = new HashSet<>();

        // TODO Improve this logic
        for (Map effect : effects) {
            int duration = (int) effect.getOrDefault("duration", 10);
            int strength = (int) effect.getOrDefault("strength", 1);
            int offset = (int) effect.getOrDefault("offset", 50); // TODO CURRENTLY IGNORED

            AscensionEffectBuilderImpl builder = buildAscensionEffects(effect);

            if (builder != null) {
                builder.setContext(context)
                        .setDuration(TickUtils.milisecondsToTicks(duration * 1000))
                        .setStrength(strength)
                        .setOffset(offset);

                ascensionEffectBuilders.add(builder);
            }
        }

        return ascensionEffectBuilders;
    }

    private AscensionEffectBuilderImpl buildAscensionEffects(Map effect) {
        AscensionEffectBuilderImpl builder = null;

        switch ((String) effect.get("name")) {
            case "BloodyAscensionEffect":
                builder = new AscensionEffectBuilderImpl.BloodyAscensionEffectBuilder();
                break;
            case "DizzyAscensionEffect":
                builder = new AscensionEffectBuilderImpl.DizzyAscensionEffectBuilder();
                break;
            case "DamagingAscensionEffect":
                builder = new AscensionEffectBuilderImpl.DamagingAscensionEffectBuilder();
                break;
            case "HallucinatingAscensionEffect":
                builder = new AscensionEffectBuilderImpl.HallucinatingAscensionEffectBuilder();
                break;
            case "SoundAscensionEffect":
                builder = new AscensionEffectBuilderImpl.SoundAscensionEffectBuilder();
                ((AscensionEffectBuilderImpl.SoundAscensionEffectBuilder) builder).setSounds((List<String>) effect.getOrDefault("sounds", null));
                break;
            case "DeathAscensionEffect":
                builder = new AscensionEffectBuilderImpl.DeathAscensionEffectBuilder();
                break;
        }
        return builder;
    }
}
