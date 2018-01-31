package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.RelicRarity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RelicType {
    Map<RelicTypeKey, RelicType> registeredRelics = new HashMap<>();

    String getName();

    List<String> getLore();

    Material getMaterial();

    short getDurability();

    RelicBehaviour getBehaviour();

    RelicRarity getRarity();


    default ItemStack getItem() {
        ItemStack item = new org.bukkit.inventory.ItemStack(getMaterial(), 1, getDurability());

        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.setLore(getLore());

        meta.setDisplayName(getRarity().getColor() + getName());


        item.setItemMeta(meta);

        return item;
    }

    default ArmorStand getAndPlaceItem(Location location){
        ItemStack item = getItem();

        ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location.add(.5, 0.6, .5).setDirection(new Vector(0, 0, 0)), EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setArms(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setHelmet(item);
        as.setRightArmPose(new EulerAngle(Math.toRadians(180), Math.toRadians(-75), Math.toRadians(90)));

        if (getRarity() == RelicRarity.SPECIAL_GRADE) {
            as.setCustomName(ChatColor.GRAY.toString() + ChatColor.MAGIC + getName());
        } else {
            as.setCustomName(ChatColor.GRAY + getName());
        }
        as.setCustomNameVisible(true);
        as.setInvulnerable(true);
        return as;
    }



    static void registerRelicType(RelicType type) {
        registeredRelics.put(type.getKey(), type);
    }

    static void unregisterAllRelics() {
        registeredRelics.clear();
    }

    static RelicType findRelicType(String name) {
        for (RelicType relicType : RelicType.registeredRelics.values()) {
            if(relicType.getName().replace(" ", "_").toLowerCase().equals(name)){
                return relicType;
            }
        }
        return null;
    }

    static RelicType getRegisteredRelicType(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return getRegisteredRelicType(itemStack.getDurability(), itemStack.getType());
    }

    static RelicType getRegisteredRelicType(Short durability, Material material) {
        return getRegisteredRelicType(new RelicTypeKey(durability, material));
    }

    static RelicType getRegisteredRelicType(RelicTypeKey key) {
        return registeredRelics.get(key);
    }

    default RelicTypeKey getKey() {
        return new RelicTypeKey(getDurability(), getMaterial());
    }


    class RelicTypeKey {
        private Short durability;
        private Material material;

        public RelicTypeKey(Short durability, Material material) {
            this.durability = durability;
            this.material = material;
        }

        @Override
        public int hashCode() {
            return durability.hashCode() ^ material.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RelicTypeKey))
                return false;
            RelicTypeKey other = (RelicTypeKey) o;
            return this.material.equals(other.material) && this.durability.equals(other.durability);
        }
    }
}
