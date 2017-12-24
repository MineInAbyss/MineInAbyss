package com.derongan.minecraft.madeinabyss.Ascension.Effect;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import org.bukkit.entity.Player;

public abstract class AbstractAscensionEffect implements AscensionEffect {
    private AbyssContext context;
    int duration;
    int strength;
    private long offset; //TODO honor this value

    AbstractAscensionEffect(AbyssContext context, long offset, int strength, int duration) {
        this.context = context;
        this.duration = duration;
        this.offset = offset;
        this.strength = strength;
    }

    AbyssContext getContext() {
        return context;
    }

    @Override
    public void applyEffect(Player player, int ticks) {
        duration -= ticks;
        applyEffect(player);
    }

    abstract void applyEffect(Player player);

    @Override
    public boolean isDone() {
        return duration <= 0;
    }

    @Override
    public int getRemainingTicks() {
        return duration;
    }

    @Override
    public void setRemainingTicks(int remainingTicks) {
        duration = remainingTicks;
    }

    @Override
    public void cleanUp(Player player) {
    }
}
