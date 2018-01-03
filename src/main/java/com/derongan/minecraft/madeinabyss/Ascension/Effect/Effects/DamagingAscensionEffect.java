package com.derongan.minecraft.madeinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamagingAscensionEffect extends AbstractAscensionEffect {
    private final PotionEffect effect = new PotionEffect(PotionEffectType.WITHER, durationRemaining, strength);

    public DamagingAscensionEffect(AbyssContext context, long offset, int strength, int duration) {
        super(context, offset, strength, duration);
    }

    @Override
    public void applyEffect(Player player) {
        if (player.getPotionEffect(PotionEffectType.WITHER) != null)
            return;
        player.addPotionEffect(effect);
    }
}
