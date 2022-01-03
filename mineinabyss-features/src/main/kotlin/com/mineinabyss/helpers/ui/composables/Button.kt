package com.mineinabyss.helpers.ui.composables

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.canvases.LocalInventory
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Composable
fun Button(item: ItemStack, modifier: Modifier = Modifier) {
    val inv = LocalInventory.current
    Item(item, modifier.clickable { //TODO clickable should pass player
        inv.viewers
            .filterIsInstance<Player>()
            .forEach { it.playSound(it.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f) }
    })
}
