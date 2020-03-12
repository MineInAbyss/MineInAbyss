package com.derongan.minecraft.mineinabyss.ascension.effect.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

//TODO not sure if anything else needs to be updated with this
public class ParticleAscensionEffect extends AbstractAscensionEffect {
    private List<Particle> particles;

    public ParticleAscensionEffect(int offset, int strength, int duration, int iterations, List<Particle> particleList) {
        super(offset, strength, duration, iterations);
        particles = particleList;
    }

    @Override
    public void applyEffect(Player player) {
        for (Particle p : particles) {
            addParticlesAroundHead(player, p);
        }
    }

    private void addParticlesAroundHead(Player player, Particle particle) {
        Location loc = player.getEyeLocation();
        float x = (float) loc.getX();
        float y = (float) loc.getY();
        float z = (float) loc.getZ();

        player.spawnParticle(particle, x, y, z, strength * 5, .5f, .5f, .5f, 0f);
    }
}
