package com.derongan.minecraft.mineinabyss.ascension.effect.effects;

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
        for(PotionEffectType potionEffectType:effectsToApply){
            PotionEffect ced = player.getPotionEffect(potionEffectType);
            int curDur = 0;
            if (ced != null) {
                curDur = ced.getDuration();
            }
            player.addPotionEffect(new PotionEffect(potionEffectType, durationRemaining+curDur, strength));
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
