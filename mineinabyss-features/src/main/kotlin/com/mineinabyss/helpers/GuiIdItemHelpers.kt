package com.mineinabyss.helpers

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.items.editItemMeta
import de.erethon.headlib.HeadLib
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object TitleItem {
    fun of(name: String, vararg lore: String) = ItemStack(Material.PAPER).editItemMeta {
        setDisplayName(name)
        setLore(lore.toList())
        setCustomModelData(0)
    }
}

@Composable
fun Text(name: String, vararg lore: String, modifier: Modifier = Modifier) {
    Item(TitleItem.of(name, *lore), modifier)
}

@Composable
fun ItemButton(name: String, vararg lore: String, customModelData: Int, modifier: Modifier = Modifier) {
    Item(
        ItemStack(Material.PAPER).editItemMeta {
            setDisplayName(name)
            setLore(lore.toList())
            setCustomModelData(customModelData)
        }, modifier)
}

// Uses a shader to hide the entire tooltip
// https://github.com/lolgeny/item-tooltip-remover
fun ItemStack.NoToolTip(): ItemStack {
    editItemMeta {
        lore = listOf("","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","                                                                                                                                                                                                                                                                                                                                                                                                                           ")
    }
    return this
}

fun OfflinePlayer?.head(
    title: String,
    vararg lore: String,
    isFlat: Boolean = false,
    isLarge: Boolean = false,
    isCenterOfInv: Boolean = false,
): ItemStack {
    this ?: return HeadLib.WOODEN_QUESTION_MARK.toItemStack()

    return ItemStack(Material.PLAYER_HEAD).editItemMeta {
        if (this is SkullMeta) {
            setDisplayName(title)
            setLore(lore.toList())
            if (isFlat) setCustomModelData(1)
            if (isLarge) setCustomModelData(2)
            if (isCenterOfInv && !isLarge) setCustomModelData(3)
            if (isCenterOfInv && isLarge) setCustomModelData(4)
            owningPlayer = this@head
        }
    }
}
