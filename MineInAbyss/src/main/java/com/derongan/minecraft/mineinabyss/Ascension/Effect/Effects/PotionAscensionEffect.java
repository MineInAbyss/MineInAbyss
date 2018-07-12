package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PotionAscensionEffect extends AbstractAscensionEffect{
    private List<PotionEffectType> effectsToApply;

    public PotionAscensionEffect(int offset, int strength, int duration, int iterations, List<PotionEffectType> effects) {
        super(offset, strength, duration, iterations);
        effectsToApply = effects;
    }

    @Override
    public void applyEffect(Player player) {
        for(PotionEffectType e:effectsToApply){
            if (player.getPotionEffect(e) != null)
                return;
            player.addPotionEffect(new PotionEffect(e, durationRemaining, strength));
        }
    }

    //TODO make potion effects be able to merge in different ways
    /** public void mergeAddPotionEffect(Player player, PotionEffectType potionEffect, int newEffectDuration, int newStrength) {
             if (player.getPotionEffect(potionEffect).getDuration() < newEffectDuration) {
                 } else if (player.getPotionEffect(potionEffect).getDuration() > newEffectDuration) {
                 } else {}
                 }**/

    public void mergeExtendPotionEffect(Player player, PotionEffectType potionEffect, int newPotionEffectDuration) {
        PotionEffect extendedPotionEffect = new PotionEffect(potionEffect, newPotionEffectDuration + player.getPotionEffect(potionEffect).getDuration() , strength);
        player.removePotionEffect(potionEffect);
        player.addPotionEffect(extendedPotionEffect);
    }
}
