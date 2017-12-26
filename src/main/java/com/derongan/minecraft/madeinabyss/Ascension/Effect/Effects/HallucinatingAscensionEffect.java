package com.derongan.minecraft.madeinabyss.Ascension.Effect.Effects;

import com.derongan.minecraft.madeinabyss.AbyssContext;
import com.derongan.minecraft.madeinabyss.Spoofing.SpoofedEntityLiving;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HallucinatingAscensionEffect extends AbstractAscensionEffect {
    private List<SpoofedEntityLiving> stands = new ArrayList<SpoofedEntityLiving>();

    public HallucinatingAscensionEffect(AbyssContext context, long offset, int strength, int duration) {
        super(context, offset, strength, duration);
    }

    @Override
    public void cleanUp(Player player) {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        stands.forEach(stand -> stand.destroy(con));
    }

    @Override
    public void applyEffect(Player player) {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        if (stands.isEmpty()) {
            for (int i = 0; i < strength; i++) {
                Location loc = player.getLocation().toVector().add(Vector.getRandom().subtract(new Vector(.5,.5,.5)).multiply(20)).toLocation(player.getWorld());
                stands.add(new SpoofedEntityLiving(getNewArmorStand(player), 78, loc));
            }
            stands.forEach(a -> {
                a.spawn(con, () -> {
                    equipStand(con, (EntityArmorStand) a.getEntity());
                });
                a.lookAt(con, player.getLocation());
            });
        } else {
            stands.forEach(a -> {
//                a.moveTo(con, player.getLocation().clone().add(1, 0, 1));
                a.lookAt(con, player.getLocation());
            });
        }
    }

    private EntityArmorStand getNewArmorStand(Player player) {
        EntityArmorStand stand = new EntityArmorStand(((CraftWorld) player.getWorld()).getHandle());
        stand.setBasePlate(true);
        Field disabledSlots;
        try {
            disabledSlots = stand.getClass().getDeclaredField("bB");
            disabledSlots.setAccessible(true);
            disabledSlots.set(stand, 2039583);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return stand;
    }

    private void moveArmorStand(PlayerConnection conn, EntityArmorStand stand) {
        long dx = (long) Math.random() * 16 - 8;
        long dy = (long) Math.random() * 16 - 8;
        long dz = (long) Math.random() * 16 - 8;


        conn.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(stand.getId(), dx * 32, dy * 32, dz, (byte) (Math.random() * 256), (byte) (Math.random() * 256), true));
    }

    private void equipStand(PlayerConnection con, EntityArmorStand stand) {
        ItemStack helm = new ItemStack(Item.getById(397));
        helm.setData(3);

        ItemStack chest = new ItemStack(Item.getById(299));
        ItemStack legs = new ItemStack(Item.getById(300));
        ItemStack boots = new ItemStack(Item.getById(301));
        ItemStack shears = new ItemStack(Item.getById(359));


        con.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), EnumItemSlot.HEAD, helm));
        con.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), EnumItemSlot.CHEST, chest));
        con.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), EnumItemSlot.LEGS, legs));
        con.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), EnumItemSlot.FEET, boots));
        con.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), EnumItemSlot.MAINHAND, shears));
        con.sendPacket(new PacketPlayOutEntityEquipment(stand.getId(), EnumItemSlot.OFFHAND, shears));
    }
}
