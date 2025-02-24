package com.mineinabyss.features.pvp.ui

import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ToggleIcon {
    val enabled = ItemStack(Material.PAPER).editItemMeta {
        setCustomModelData(2)
        itemName("<blue><b>Toggle PvP Prompt".miniMsg())
        lore(
            listOf(
                "<red>Disable <dark_aqua>this prompt from showing".miniMsg(),
                "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
                "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
            )
        )
    }

    val disabled =
        ItemStack(Material.PAPER).editItemMeta {
            setCustomModelData(3)
            itemName("<blue><b>Toggle PvP Prompt".miniMsg())
            lore(
                listOf(
                    "<green>Enable <dark_aqua>this prompt from showing".miniMsg(),
                    "<dark_aqua>when entering the <green>Abyss.".miniMsg(),
                    "<dark_aqua>It can be re-opened at any time in <gold>Orth.".miniMsg()
                )
            )
        }
}
