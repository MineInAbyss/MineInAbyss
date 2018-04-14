package com.derongan.minecraft.mineinabyss.Effect.Curse;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BloodyCurseEffect extends CurseEffect {
    @Override
    public void apply(Player entity, int strength) {
        Location loc = entity.getEyeLocation();

        float x = (float) loc.getX();
        float y = (float) loc.getY();
        float z = (float) loc.getZ();
        PacketPlayOutWorldParticles p = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true, x, y, z, .5f, .5f, .5f, 0f, strength*10);
        ((CraftPlayer) entity).getHandle().playerConnection.sendPacket(p);
    }
}
