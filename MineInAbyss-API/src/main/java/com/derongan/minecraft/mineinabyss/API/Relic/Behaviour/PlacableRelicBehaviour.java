package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import com.derongan.minecraft.mineinabyss.API.Relic.RelicRarity;
import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Behaviour that fires when a registered armorstand is interacted with
 */
public interface PlacableRelicBehaviour extends RelicBehaviour{
    static ArmorStand getAndPlaceItem(RelicType relic, Location location){
        ItemStack item = relic.getItem();

        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(.5, 0.6, .5).setDirection(new Vector(0, 0, 0)), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setArms(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setHelmet(item);
        //as.setRightArmPose(new EulerAngle(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0)));

        if (relic.getRarity() == RelicRarity.SPECIAL_GRADE) {
            as.setCustomName(ChatColor.GRAY.toString() + ChatColor.MAGIC + relic.getName());
        } else {
            as.setCustomName(ChatColor.GRAY + relic.getName());
        }
        as.setCustomNameVisible(true);
        as.setInvulnerable(true);
        return as;
    }
    static ArmorStand getAndPlaceItem(RelicType relic, Location location, double offsetX, double offsetY, double offsetZ, double rotX, double rotY, double rotZ){
        ItemStack item = relic.getItem();

        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(offsetX, offsetY, offsetZ).setDirection(new Vector(0, 0, 0)), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setArms(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setHelmet(item);
        as.setRightArmPose(new EulerAngle(Math.toRadians(rotX), Math.toRadians(rotY), Math.toRadians(rotZ)));

        if (relic.getRarity() == RelicRarity.SPECIAL_GRADE) {
            as.setCustomName(ChatColor.GRAY.toString() + ChatColor.MAGIC + relic.getName());
        } else {
            as.setCustomName(ChatColor.GRAY + relic.getName());
        }
        as.setCustomNameVisible(true);
        as.setInvulnerable(true);
        return as;
    }
}
