package com.mineinabyss.npc.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.*
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.helpers.ui.rememberNavigation
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.ChatColor.WHITE
import org.bukkit.entity.Player

sealed class BankScreen(val title: String, val height: Int) {
    object Default : BankScreen("${Space.of(-18)}$WHITE:orthbanking_menu:", 4)
    object Deposit : BankScreen("${Space.of(-18)}$WHITE:orthbanker_deposit_menu:", 5)
    object Widthdraw : BankScreen("${Space.of(18)}$WHITE:orthbanker_withdrawal_menu:", 5)
}

@Composable
fun GuiyOwner.BankMenu(player: Player) {
    val nav = rememberNavigation<BankScreen> { BankScreen.Default }
    nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
        Chest(setOf(player), screen.title, Modifier.height(screen.height),
            onClose = {
                player.updateBalance()
                nav.back()
            }) {
            when (screen) {
                BankScreen.Default -> {
                    val data = player.playerData
                    DepositCurrencyOption(data, Modifier.at(1, 1).clickable {
                        nav.open(BankScreen.Deposit)
                    })
                    WithdrawCurrencyOption(data, Modifier.at(5, 1).clickable {
                        nav.open(BankScreen.Widthdraw)
                    })
                }
                BankScreen.Deposit -> DepositScreen(player)
                BankScreen.Widthdraw -> WithdrawScreen(player)
            }
        }
    }
}

@Composable
fun DepositCurrencyOption(data: PlayerData, modifier: Modifier = Modifier) = Button {
    Text(
        "<gold><b>Open Deposit Menu".miniMsg(),
        "<yellow>You currently have <i>${data.orthCoinsHeld} <yellow>coins in your account.".miniMsg(),
        modifier = modifier.size(3, 2)
    )
}

@Composable
fun WithdrawCurrencyOption(data: PlayerData, modifier: Modifier = Modifier) = Button {
    Text(
        "<gold><b>Open Withdrawal Menu".miniMsg(),
        "<yellow>You currently have <i>${data.orthCoinsHeld} <yellow>coins in your account.".miniMsg(),
        modifier = modifier.size(3, 2)
    )
}

