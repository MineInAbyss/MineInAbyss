package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import org.bukkit.entity.Player;

public class DeathAscensionEffect extends AbstractAscensionEffect {
    public DeathAscensionEffect(long offset, int strength, int duration) {
        super(offset, strength, duration);
    }

    @Override
    void applyEffect(Player player) {
        player.setHealth(0.0D);
    }
}
