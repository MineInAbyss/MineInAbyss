package com.derongan.minecraft.madeinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BloodyAscensionEffect extends AbstractAscensionEffect {
    public BloodyAscensionEffect(AbyssContext context, long offset, int strength, int duration) {
        super(context, offset, strength, duration);
    }

    @Override
    public void applyEffect(Player player) {
        addBloodParticlesAroundHead(player);
    }

    private void addBloodParticlesAroundHead(Player player) {
        Location loc = player.getEyeLocation();

        float x = (float) loc.getX();
        float y = (float) loc.getY();
        float z = (float) loc.getZ();
        PacketPlayOutWorldParticles p = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, x, y, z, .5f, .5f, .5f, 0f, (int)strength*10);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(p);
    }

}
