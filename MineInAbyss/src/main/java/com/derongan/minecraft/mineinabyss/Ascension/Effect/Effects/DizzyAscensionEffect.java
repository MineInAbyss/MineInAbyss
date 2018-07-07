package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DizzyAscensionEffect extends AbstractAscensionEffect {
    private final PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, durationRemaining, strength);

    public DizzyAscensionEffect(int offset, int strength, int duration, int iterations) {
        super(offset, strength, duration, iterations);
    }

    @Override
    public void applyEffect(Player player) {
        if (player.getPotionEffect(PotionEffectType.CONFUSION) != null)
            return;
        player.addPotionEffect(effect);
    }

    @Override
    public void cleanUp(Player player) {
        super.cleanUp(player);
        player.removePotionEffect(PotionEffectType.CONFUSION);
    }
}
            