package com.derongan.minecraft.madeinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import org.bukkit.entity.Player;

public class DeathAscensionEffect extends AbstractAscensionEffect {
    public DeathAscensionEffect(AbyssContext context, long offset, int strength, int duration) {
        super(context, offset, strength, duration);
    }

    @Override
    void applyEffect(Player player) {
        player.setHealth(0.0D);
    }
}
