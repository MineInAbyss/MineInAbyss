package com.derongan.minecraft.mineinabyss.Relic.Relics;

import com.derongan.minecraft.mineinabyss.Relic.Behaviour.RelicBehaviour;
import com.derongan.minecraft.mineinabyss.Relic.RelicRarity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    static void registerRelicType(RelicType type) {
        registeredRelics.put(type.getKey(), type);
    }

    static void unregisterAllRelics() {
        registeredRelics.clear();
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
