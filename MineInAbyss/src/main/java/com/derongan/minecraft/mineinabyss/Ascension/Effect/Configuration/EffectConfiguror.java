package com.derongan.minecraft.mineinabyss.Ascension.Effect.Configuration;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffect;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffectBuilder;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffectBuilderImpl;
import com.derongan.minecraft.mineinabyss.util.TickUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class EffectConfiguror {

    public static AscensionEffectBuilder createBuilderFromMap(Map effect) {
        int duration = (int) effect.getOrDefault("duration", 10);
        int strength = (int) effect.getOrDefault("strength", 1);
        int offset = (int) effect.getOrDefault("offset", 0);
        int iterations = (int) effect.getOrDefault("iterations", 1);

        AscensionEffectBuilderImpl builder = buildAscensionEffects(effect);

        if (builder != null) {
            builder.setDuration(TickUtils.milisecondsToTicks(duration * 1000))
                    .setStrength(strength)
                    .setIterations(iterations)
                    .setOffset(TickUtils.milisecondsToTicks(offset * 1000));
        }

        return builder;
    }

    private static AscensionEffectBuilderImpl buildAscensionEffects(Map effect) {
        AscensionEffectBuilderImpl builder = null;

        switch ((String) effect.get("name")) {
            case "PotionAscensionEffect":
                builder = new AscensionEffectBuilderImpl.PotionAscensionEffectBuilder()
                        .setEffects((List<String>) effect.getOrDefault("effects", null));
                break;
            case "ParticleAscensionEffect":
                builder = new AscensionEffectBuilderImpl.ParticleAscensionEffectBuilder()
                        .setParticles((List<String>) effect.getOrDefault("particles", null));
                break;
            case "HallucinatingAscensionEffect":
                builder = new AscensionEffectBuilderImpl.HallucinatingAscensionEffectBuilder();
                break;
            case "SoundAscensionEffect":
                builder = new AscensionEffectBuilderImpl.SoundAscensionEffectBuilder()
                        .setSounds((List<String>) effect.getOrDefault("sounds", null));
                break;
            case "DeathAscensionEffect":
                builder = new AscensionEffectBuilderImpl.DeathAscensionEffectBuilder();
                break;
        }
        return builder;
    }
}
