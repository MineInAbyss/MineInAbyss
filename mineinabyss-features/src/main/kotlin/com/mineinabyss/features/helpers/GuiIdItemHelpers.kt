package com.mineinabyss.features.helpers

import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object TitleItem {
    @Deprecated("Use Guiy's text component instead, this remains for AnvilGUI, with a rewrite soon", replaceWith = ReplaceWith("Text"))
    fun of(name: String, vararg lore: String) = ItemStack.of(Material.PAPER).editItemMeta {
        itemName(name.miniMsg())
        lore(lore.toList().map { it.miniMsg() })
        setCustomModelData(1)
    }

    val transparentItem = ItemStack.of(Material.PAPER).editItemMeta {
        setCustomModelData(1)
        isHideTooltip = true
    }
}
