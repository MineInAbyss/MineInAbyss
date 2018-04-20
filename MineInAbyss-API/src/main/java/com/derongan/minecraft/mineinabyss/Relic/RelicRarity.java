package com.derongan.minecraft.mineinabyss.Relic;

import org.bukkit.ChatColor;

public enum RelicRarity {
    SPECIAL_GRADE(ChatColor.LIGHT_PURPLE),
    FIRST_GRADE(ChatColor.RED),
    SECOND_GRADE(ChatColor.GOLD),
    THIRD_GRADE(ChatColor.DARK_GREEN),
    FOURTH_GRADE(ChatColor.DARK_BLUE),
    TOOL(ChatColor.GRAY),
    NOT_IMPLEMENTED(ChatColor.WHITE);

    ChatColor color;

    RelicRarity(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }
}
