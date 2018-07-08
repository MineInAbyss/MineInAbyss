package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamagingAscensionEffect extends AbstractAscensionEffect {
    private final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, durationRemaining, strength);

    public DamagingAscensionEffect(int offset, int strength, int duration, int iterations) {
        super(offset, strength, duration, iterations);
    }

    @Override
    public void applyEffect(Player player) {
        if (player.getPotionEffect(PotionEffectType.WITHER) != null)
            return;
        player.addPotionEffect(effect);
    }
}
