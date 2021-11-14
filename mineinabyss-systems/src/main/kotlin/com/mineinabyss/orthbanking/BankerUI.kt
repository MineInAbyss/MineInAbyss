package com.mineinabyss.orthbanking

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.idofront.font.NegativeSpace
import org.bukkit.ChatColor
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.BankUI(player: Player) {
    Chest(
        listOf(player), NegativeSpace.of(18) + "${ChatColor.WHITE}:orthbanker:",
        4, onClose = { reopen() }) {
        BankerMenu(player)
    }
}

@Composable
fun BankerMenu(player: Player) {

}