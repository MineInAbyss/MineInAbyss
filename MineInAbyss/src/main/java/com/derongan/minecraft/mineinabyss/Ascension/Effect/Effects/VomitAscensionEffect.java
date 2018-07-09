package com.derongan.minecraft.mineinabyss.Ascension.Effect.Effects;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class VomitAscensionEffect extends AbstractAscensionEffect {

    public VomitAscensionEffect(int offset, int strength, int duration, int iterations) {
        super(offset, strength, duration, iterations);
    }

    @Override
    public void applyEffect(Player player) {
        addVomitParticlesAroundHead(player);
    }

    private void addVomitParticlesAroundHead (Player player) {
        Location loc = player.getEyeLocation();

        float x = (float) loc.getX();
        float y = (float) loc.getY();
        float z = (float) loc.getZ();

        PacketPlayOutWorldParticles p = new PacketPlayOutWorldParticles(EnumParticle.SLIME, true, x, y, z, .5f, .5f, .5f, 0f, (int)strength*20);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(p);
    }
}
