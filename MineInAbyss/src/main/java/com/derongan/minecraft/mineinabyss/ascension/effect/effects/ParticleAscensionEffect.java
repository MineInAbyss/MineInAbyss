package com.derongan.minecraft.mineinabyss.ascension.effect.effects;

import org.bukkit.entity.Player;

import java.util.List;

//TODO fix for 1.13
public class ParticleAscensionEffect extends AbstractAscensionEffect {
//    private List<EnumParticle> particles;

    public ParticleAscensionEffect(int offset, int strength, int duration, int iterations, List<Object> particleList) {
        super(offset, strength, duration, iterations);
//        particles = particleList;
    }

    @Override
    public void applyEffect(Player player) {
//        for(EnumParticle p:particles){ addParticlesAroundHead(player, p); }
    }

//    private void addParticlesAroundHead (Player player, EnumParticle particle) {
//        Location loc = player.getEyeLocation();
//
//        float x = (float) loc.getX();
//        float y = (float) loc.getY();
//        float z = (float) loc.getZ();
//
//        PacketPlayOutWorldParticles p = new PacketPlayOutWorldParticles(particle, true, x, y, z, .5f, .5f, .5f, 0f, (int)strength*5);
//
//        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(p);
//    }
}
