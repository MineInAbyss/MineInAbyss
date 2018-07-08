package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import org.bukkit.entity.Player;

public class DeathAscensionEffect extends AbstractAscensionEffect {
    public DeathAscensionEffect(int offset, int strength, int duration, int iterations) {
        super(offset, strength, duration, iterations);
    }

    @Override
    void applyEffect(Player player) {
        player.setHealth(0.0D);
    }
}
