package com.derongan.minecraft.mineinabyss.Relic.Relics;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractRelicType {
    Material material;
    short durability;

    ItemMeta unbreakableMeta;

    /**
     * Relic type has material for base item, and durability for what durability equals that item
     *
     * @param material
     * @param durability
     */
    public AbstractRelicType(Material material, int durability) {
        this.material = material;
        this.durability = (short)durability;
    }


    public ItemStack getItem() {
        ItemStack item = new ItemStack(material, 1, durability);

        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.setDisplayName(getName());
        meta.setLore(getLore());

        item.setItemMeta(meta);

        return item;
    }


    public String getName() {
        return "Relic";
    }

    public List<String> getLore() {
        return Arrays.asList("An amazing relic");
    }

    public abstract void onUse(PlayerInteractEvent event);

    public boolean isRelicItem(ItemStack itemStack){
        return itemStack.getDurability() == durability;
    }
}
