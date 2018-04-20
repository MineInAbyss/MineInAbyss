package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamagingAscensionEffect extends AbstractAscensionEffect {
    private final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, durationRemaining, strength);

    public DamagingAscensionEffect(long offset, int strength, int duration) {
        super(offset, strength, duration);
    }

    @Override
    public void applyEffect(Player player) {
        if (player.getPotionEffect(PotionEffectType.WITHER) != null)
            return;
        player.addPotionEffect(effect);
    }
}
