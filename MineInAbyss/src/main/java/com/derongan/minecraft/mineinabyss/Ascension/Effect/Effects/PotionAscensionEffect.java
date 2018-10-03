package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.mineinabyss.MineInAbyss;
import org.bukkit.Bukkit;
import java.util.Arrays;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PotionAscensionEffect extends AbstractAscensionEffect{
    private List<PotionEffectType> effectsToApply;
    private static final List<PotionEffectType> notSummable = Arrays.asList(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.GLOWING,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.POISON);

    public PotionAscensionEffect(int offset, int strength, int duration, int iterations, List<PotionEffectType> effects) {
        super(offset, strength, duration, iterations);
        effectsToApply = effects;
    }

    @Override
    public void applyEffect(Player player) {
        for(PotionEffectType e:effectsToApply){
            if (player.getPotionEffect(e) != null) {
                mergeExtendPotionEffect(player, e, durationRemaining);
            } else {
                player.addPotionEffect(new PotionEffect(e, durationRemaining, strength));
            }
        }
    }

    //potion effect merge shenanigans, creates a new stronger effect for when the effects overlap and another effect for the longer effect
    /*private void mergeAddPotionEffect(Player player, PotionEffectType potionEffect, int newEffectDuration, int newStrength) {
        if (player.getPotionEffect(potionEffect).getDuration() < newEffectDuration) {
            int oldEffectDuration = player.getPotionEffect(potionEffect).getDuration();
            int oldEffectStrength = player.getPotionEffect(potionEffect).getAmplifier();

            PotionEffect firstAddPotionEffect = new PotionEffect(potionEffect, oldEffectDuration, player.getPotionEffect(potionEffect).getAmplifier() + newStrength);

            player.removePotionEffect(potionEffect);
            player.addPotionEffect(firstAddPotionEffect);

            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
                if (player.getPotionEffect(potionEffect) != null) {
                    mergeAddPotionEffect(player, potionEffect, newEffectDuration - oldEffectDuration, newStrength);
                } else {
                    player.addPotionEffect(new PotionEffect(potionEffect, newEffectDuration - oldEffectDuration, newStrength));
                }
                }, (oldEffectDuration + 1));

        } else if (player.getPotionEffect(potionEffect).getDuration() > newEffectDuration) {
            int oldEffectDuration = player.getPotionEffect(potionEffect).getDuration();
            int oldEffectStrength = player.getPotionEffect(potionEffect).getAmplifier();

            PotionEffect firstAddPotionEffect = new PotionEffect(potionEffect, newEffectDuration, oldEffectStrength + newStrength);

            player.removePotionEffect(potionEffect);
            player.addPotionEffect(firstAddPotionEffect);

            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(MineInAbyss.class), () -> {
                if (player.getPotionEffect(potionEffect) != null) {
                    mergeAddPotionEffect(player, potionEffect,  oldEffectDuration - newEffectDuration, oldEffectStrength);
                } else {
                    player.addPotionEffect(new PotionEffect(potionEffect, oldEffectDuration - newEffectDuration, oldEffectStrength));
                }
            }, (newEffectDuration + 1));

        } else {
            PotionEffect mergedAddPotionEffect =  new PotionEffect(potionEffect, newEffectDuration, player.getPotionEffect(potionEffect).getAmplifier() + newStrength);
            player.removePotionEffect(potionEffect);
            player.addPotionEffect(mergedAddPotionEffect);
        }
    }*/

    //if the effects merging strength makes no difference
    private void mergeExtendPotionEffect(Player player, PotionEffectType potionEffect, int newEffectDuration) {
        PotionEffect extendedPotionEffect = new PotionEffect(potionEffect, newEffectDuration + player.getPotionEffect(potionEffect).getDuration() + 10, strength);
        player.removePotionEffect(potionEffect);
        player.addPotionEffect(extendedPotionEffect);
    }
}
