package com.derongan.minecraft.mineinabyss.plugin.Relic.Relics;

import com.derongan.minecraft.mineinabyss.API.Relic.Relics.RelicType;
import com.derongan.minecraft.mineinabyss.plugin.Relic.Behaviour.Behaviours.LootableRelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.API.Relic.RelicRarity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * A "relic" used for spawned loot
 */
public class LootableRelicType implements RelicType {
    LootableRelicBehaviour behaviour = new LootableRelicBehaviour();

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<String> getLore() {
        return null;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    //TODO maybe have a base relic type with no item info
    @Override
    public short getDurability() {
        return 0;
    }

    @Override
    public RelicBehaviour getBehaviour() {
        return behaviour;
    }

    @Override
    public RelicRarity getRarity() {
        return null;
    }

    public void spawnLootableRelic(Location location, RelicType type, int lifetime) {
        ItemStack item = type.getItem();

        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(.5, -1.4, .5).setDirection(new Vector(0, 0, 0)), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setArms(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setItemInHand(item);
        as.setRightArmPose(new EulerAngle(-Math.PI / 2, -Math.PI / 2, 0));

        if (type.getRarity() == RelicRarity.SPECIAL_GRADE) {
            as.setCustomName(ChatColor.GRAY.toString() + ChatColor.MAGIC + type.getName());
        } else {
            as.setCustomName(ChatColor.GRAY + type.getName());
        }
        as.setCustomNameVisible(true);
        as.setInvulnerable(true);

        behaviour.registerRelic(as, lifetime, this);
        behaviour.registerRelic(as.getUniqueId(), this);
    }
}
