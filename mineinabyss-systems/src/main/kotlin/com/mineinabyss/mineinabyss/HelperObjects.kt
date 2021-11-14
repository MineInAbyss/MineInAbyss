package com.mineinabyss.mineinabyss

import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ItemModels {
    val InvisPaper = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(1)
        setDisplayName("${ChatColor.DARK_GREEN}${ChatColor.BOLD}Enable PvP")
        lore = mutableListOf("${ChatColor.GREEN}Enables pvp interactions with other players in the Abyss.")
    }

}