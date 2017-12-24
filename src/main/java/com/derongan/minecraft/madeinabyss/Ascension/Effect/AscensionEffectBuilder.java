package com.derongan.minecraft.madeinabyss.Ascension.Effect;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import org.bukkit.Sound;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


//TODO look at java patterns and figure out a nice way to not have to repeat the builder multiple times
public abstract class AscensionEffectBuilder<E extends AscensionEffect> {
    private AbyssContext context;
    private long offset = 30;
    private int strength = 1;
    private int duration = 200;   // Pass it in as as ticks here

    public abstract E build();

    public AscensionEffectBuilder<E> setContext(AbyssContext context) {
        this.context = context;
        return this;
    }

    AbyssContext getContext() {
        return context;
    }

    long getOffset() {
        return offset;
    }

    int getStrength() {
        return strength;
    }

    public AscensionEffectBuilder<E> setStrength(int strength) {
        this.strength = strength;
        return this;
    }

    public AscensionEffectBuilder<E> setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    int getDuration() {
        return duration;
    }

    public AscensionEffectBuilder<E> setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public static class BloodyAscensionEffectBuilder extends AscensionEffectBuilder {
        @Override
        public AscensionEffect build() {
            return new BloodyAscensionEffect(getContext(), getOffset(), getStrength(), getDuration());
        }
    }

    public static class DamagingAscensionEffectBuilder extends AscensionEffectBuilder<BloodyAscensionEffect> {
        @Override
        public BloodyAscensionEffect build() {
            return new BloodyAscensionEffect(getContext(), getOffset(), getStrength(), getDuration());
        }
    }

    public static class DizzyAscensionEffectBuilder extends AscensionEffectBuilder<DizzyAscensionEffect> {
        @Override
        public DizzyAscensionEffect build() {
            return new DizzyAscensionEffect(getContext(), getOffset(), getStrength(), getDuration());
        }
    }

    public static class
    HallucinatingAscensionEffectBuilder extends AscensionEffectBuilder<HallucinatingAscensionEffect> {
        @Override
        public HallucinatingAscensionEffect build() {
            return new HallucinatingAscensionEffect(getContext(), getOffset(), getStrength(), getDuration());
        }
    }

    public static class SoundAscensionEffectBuilder extends AscensionEffectBuilder<SoundAscensionEffect> {
        private static final List<String> ALL_SOUNDS = Arrays.stream(Sound.values()).map(sound->sound.name()).collect(Collectors.toList());
        List<String> sounds = ALL_SOUNDS;

        @Override
        public SoundAscensionEffect build() {
            return new SoundAscensionEffect(getContext(), getOffset(), getStrength(), getDuration(), getSounds());
        }

        public SoundAscensionEffectBuilder setSounds(List<String> allowedSounds){
            sounds = allowedSounds;
            return this;
        }

        List<String> getSounds() {
            return sounds;
        }
    }
}
