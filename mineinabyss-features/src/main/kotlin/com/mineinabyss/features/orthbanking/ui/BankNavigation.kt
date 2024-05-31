package com.mineinabyss.features.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.playerData
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.rememberNavigation
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

sealed class BankScreen(val title: String, val height: Int) {
    object Default : BankScreen(":space_-8::orthbanking_menu:", 4)
    object Deposit : BankScreen(":space_-8::orthbanker_deposit_menu:", 5)
    object Widthdraw : BankScreen(":space_-8::orthbanker_withdrawal_menu:", 5)
}

@Composable
fun GuiyOwner.BankMenu(player: Player) {
    val nav = rememberNavigation<BankScreen> { BankScreen.Default }
    nav.withScreen(setOf(player), onEmpty = ::exit) { screen ->
        Chest(setOf(player), screen.title, Modifier.height(screen.height),
            onClose = { nav.back() }) {
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

