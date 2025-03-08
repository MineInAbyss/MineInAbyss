package com.mineinabyss.features.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

sealed class BankScreen(val title: String, val height: Int) {
    data object Default : BankScreen(":space_-8::orthbanking_menu:", 4)
    data object Deposit : BankScreen(":space_-8::orthbanker_deposit_menu:", 5)
    data object Widthdraw : BankScreen(":space_-8::orthbanker_withdrawal_menu:", 5)
}

@Composable
fun GuiyOwner.BankMenu(player: Player) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = BankScreen.Default) {
        //TODO move chest into composables
//        Chest(screen.title, Modifier.height(screen.height), onClose = { nav.back() }) {
        composable<BankScreen.Default> {
            val data = player.playerDataOrNull ?: PlayerData() // careful not to modify directly here
            DepositCurrencyOption(data, Modifier.at(1, 1).clickable {
//                nav.open(BankScreen.Deposit) //TODO navigation
            })
            WithdrawCurrencyOption(data, Modifier.at(5, 1).clickable {
//                nav.open(BankScreen.Widthdraw)
            })

        }
        composable<BankScreen.Deposit> { DepositScreen(player) }
        composable<BankScreen.Widthdraw> { WithdrawScreen(player) }
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

