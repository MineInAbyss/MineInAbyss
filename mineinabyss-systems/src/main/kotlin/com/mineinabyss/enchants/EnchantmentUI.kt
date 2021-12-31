package com.mineinabyss.enchants

import androidx.compose.runtime.Composable
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.idofront.font.Space
import org.bukkit.ChatColor
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.EnchantmentTableMenu(player: Player) {
    val gearyPlayer = player.toGearyOrNull() ?: return

    Chest(setOf(player), "${Space.of(-20)}${ChatColor.WHITE}:enchantmentui:", Modifier.height(4)) {

    }
}
