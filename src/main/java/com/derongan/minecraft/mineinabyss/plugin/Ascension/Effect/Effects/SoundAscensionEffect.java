package com.derongan.minecraft.mineinabyss.plugin.Ascension.Effect.Effects;

import com.derongan.minecraft.mineinabyss.plugin.AbyssContext;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.util.stream.Collectors.toList;


public class SoundAscensionEffect extends AbstractAscensionEffect {
    private List<Sound> sounds;
    private static Random random = new Random();

    private int lastDurationRemaining;
    private int ticksBetweenSoundInitialization;

    public SoundAscensionEffect(AbyssContext context, long offset, int strength, int duration, List<String> allowedSounds) {
        super(context, offset, strength, duration);
        sounds = allowedSounds.stream().map((soundString) -> {
            try {
                return Sound.valueOf(soundString);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }).filter(Objects::nonNull).collect(toList());
        lastDurationRemaining = durationRemaining;
        ticksBetweenSoundInitialization = strength;
    }

    @Override
    void applyEffect(Player player) {
        if (lastDurationRemaining - durationRemaining >= ticksBetweenSoundInitialization) {
            Location soundLocation = Vector.getRandom().multiply(5).subtract(new Vector(2.5, 2.5, 2.5)).toLocation(player.getWorld()).add(player.getLocation());
            Sound sound = sounds.get(random.nextInt(sounds.size()));

            getContext().getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getContext().getPlugin(), () -> {
                player.playSound(soundLocation, sound, 1f, 1f);
            }, random.nextInt(getContext().getTickTime()));
            lastDurationRemaining = durationRemaining;
        }
    }
}
