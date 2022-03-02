package com.mineinabyss.helpers.ui.composables

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.canvases.LocalInventory
import com.mineinabyss.guiy.layout.Row
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.clickable
import org.bukkit.Sound
import org.bukkit.entity.Player

@Composable
inline fun Button(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    crossinline onClick: () -> Unit = {},
    playSound: Boolean = true,
    crossinline content: @Composable (enabled: Boolean) -> Unit,
) {
    val inv = LocalInventory.current
    Row(modifier.clickable { //TODO clickable should pass player
        val viewers = inv.viewers.filterIsInstance<Player>()
        if (playSound) {
            if (enabled) viewers.forEach { it.playSound(it.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f) }
            else viewers.forEach { it.playSound(it.location, Sound.BLOCK_LEVER_CLICK, 1f, 1f) }
        }
        if (enabled) onClick()
    }) {
        content(enabled)
    }
}
