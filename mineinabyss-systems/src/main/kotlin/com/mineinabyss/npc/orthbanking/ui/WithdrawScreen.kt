package com.mineinabyss.npc.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.clickable
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.npc.orthbanking.withdrawCoins
import org.bukkit.ChatColor
import org.bukkit.entity.Player

@Composable
fun WithdrawScreen(player: Player) {
    var amount = 1

    Button(
        TitleItem.of("${ChatColor.GOLD}${ChatColor.BOLD}Increase Withdrawal"),
        Modifier.size(3, 2).at(3, 0).clickable {
            amount += 1
            if (amount > 64) amount = 64
            broadcast(amount)
        }
    )

    Button(
        TitleItem.of("${ChatColor.GOLD}${ChatColor.BOLD}Confirm Withdrawal"),
        Modifier.at(4, 2).clickable {
            broadcast(amount)
            player.withdrawCoins(amount)
            player.updateBalance()
            player.closeInventory()
        }
    )

    Button(
        TitleItem.of("${ChatColor.GOLD}${ChatColor.BOLD}Decrease Withdrawal"),
        Modifier.size(3, 1).at(3, 3).clickable {
            amount -= 1
            if (amount < 1) amount = 1
            broadcast(amount)
        })

}
