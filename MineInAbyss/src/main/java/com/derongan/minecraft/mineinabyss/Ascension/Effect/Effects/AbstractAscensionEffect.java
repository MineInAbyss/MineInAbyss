package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import com.derongan.minecraft.mineinabyss.Ascension.Effect.AscensionEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.derongan.minecraft.mineinabyss.MineInAbyss;

import org.bukkit.plugin.java.JavaPlugin;


public abstract class AbstractAscensionEffect implements AscensionEffect {
    int durationRemaining;
    int elapsed;
    int strength;
    int offset;
    int iterations;

    AbstractAscensionEffect(int offset, int strength, int durationRemaining, int iterations) {
        this.iterations = iterations;
        this.durationRemaining = durationRemaining;
        this.elapsed = 0;
        this.offset = offset;
        this.strength = strength;
    }

    @Override
    public void applyEffect(Player player, int ticks) {
        for (int iterationsScheduled = 0; iterationsScheduled != iterations; iterationsScheduled++) {
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
                applyEffect(player);
            }, (offset + iterationsScheduled + (durationRemaining * iterationsScheduled)));
        }
    }

    abstract void applyEffect(Player player);

    @Override
    public boolean isDone() {
        return durationRemaining <= 0;
    }

    @Override
    public int getRemainingTicks() {
        return durationRemaining;
    }

    @Override
    public void setRemainingTicks(int remainingTicks) {
        durationRemaining = remainingTicks;
    }

    @Override
    public void cleanUp(Player player) {
    }
}
