package com.mineinabyss.helpers

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.textcomponents.miniMsg
import de.erethon.headlib.HeadLib
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object TitleItem {
    fun of(name: String, vararg lore: String) = ItemStack(Material.PAPER).editItemMeta {
        displayName(name.miniMsg())
        setLore(lore.toList())
        setCustomModelData(1)
    }
    fun of(name: Component, vararg lore: Component) = ItemStack(Material.PAPER).editItemMeta {
        displayName(name)
        lore(lore.toList())
        setCustomModelData(1)
    }
}

@Composable
fun Text(name: String, vararg lore: String, modifier: Modifier = Modifier) {
    Item(TitleItem.of(name, *lore), modifier)
}

@Composable
fun Text(name: Component, vararg lore: Component, modifier: Modifier = Modifier) {
    Item(TitleItem.of(name, *lore), modifier)
}

fun OfflinePlayer?.head(
    title: Component,
    vararg lore: Component,
    isFlat: Boolean = false,
    isLarge: Boolean = false,
    isCenterOfInv: Boolean = false,
): ItemStack {
    this ?: return HeadLib.WOODEN_QUESTION_MARK.toItemStack()

    return ItemStack(Material.PLAYER_HEAD).editItemMeta {
        if (this is SkullMeta) {
            displayName(title)
            lore(lore.toList())
            if (isFlat) setCustomModelData(10)
            if (isLarge) setCustomModelData(11)
            if (isCenterOfInv && !isLarge) setCustomModelData(12)
            if (isCenterOfInv && isLarge) setCustomModelData(13)
            owningPlayer = this@head
        }
    }
}
