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
    private int iterationsRemaining;

    AbstractAscensionEffect(int offset, int strength, int durationRemaining, int iterations) {
        this.iterations = iterations;
        this.durationRemaining = durationRemaining;
        this.elapsed = 0;
        this.offset = offset;
        this.strength = strength;
        this.iterationsRemaining = iterations;
    }

    @Override
    public void applyEffect(Player player, int ticks) {
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
            applyEffect(player);
            if (iterationsRemaining > 1) {
                Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
                    repeatEffect(player);
                }, (1 + durationRemaining));
            }
        }, (offset));

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

    private void repeatEffect(Player player){
        iterationsRemaining--;

        if(iterationsRemaining < 1){
            applyEffect(player);
        }

        if (iterationsRemaining > 0) {
            applyEffect(player);
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
                repeatEffect(player);
            }, (1 + durationRemaining));
        }
    }
}