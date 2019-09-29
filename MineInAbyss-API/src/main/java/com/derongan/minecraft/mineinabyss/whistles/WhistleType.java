package com.derongan.minecraft.mineinabyss.whistles;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public enum WhistleType {
    BELL("Bell", 1),
    RED_WHISTLE("Red Whistle", 2),
    BLUE_WHISTLE("Blue Whistle", 3),
    MOON_WHISTLE("Moon Whistle", 4),
    BLACK_WHISTLE("Black Whistle", 5),
    WHITE_WHISTLE("White Whistle", 6);

    private String name;
    private int damageValue;

    WhistleType(String name, int damageValue) {
        this.name = name;
        this.damageValue = damageValue;
    }

    public ItemStack getItem() {
        ItemStack whistle = new ItemStack(Material.DIAMOND_HOE, 1);
        ItemMeta meta = whistle.getItemMeta();
        meta.setDisplayName(name);
        ((Damageable) meta).setDamage(damageValue);
        meta.setUnbreakable(true);
        whistle.setItemMeta(meta);
        return whistle;
    }
}
