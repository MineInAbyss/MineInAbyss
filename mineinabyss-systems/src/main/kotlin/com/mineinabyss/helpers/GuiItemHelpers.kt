package com.mineinabyss.helpers

import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TitleItem {
    fun of(name: String, vararg lore: String) = ItemStack(Material.PAPER).editItemMeta {
        setDisplayName(name)
        setLore(lore.toList())
        setCustomModelData(1)
    }
}
