package com.mineinabyss.helpers

import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ItemModels {
    val InvisPaper = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(1)
    }

}
