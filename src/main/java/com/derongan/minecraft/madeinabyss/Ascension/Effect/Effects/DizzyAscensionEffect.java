package com.derongan.minecraft.madeinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DizzyAscensionEffect extends AbstractAscensionEffect {
    private final PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, duration, strength);

    public DizzyAscensionEffect(AbyssContext context, long offset, int strength, int duration) {
        super(context, offset, strength, duration);
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
