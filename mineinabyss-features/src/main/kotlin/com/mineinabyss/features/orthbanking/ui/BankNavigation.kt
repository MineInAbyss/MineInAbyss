package com.mineinabyss.features.orthbanking.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.entity.Player

sealed class BankScreen {
    data object Default : BankScreen()
    data object Deposit : BankScreen()
    data object Widthdraw : BankScreen()
}

@Composable
fun BankMenu(player: Player) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = BankScreen.Default) {
        composable<BankScreen.Default> {
            Chest(":space_-8::orthbanking_menu:", Modifier.height(4.dp)) {
                val data = player.playerDataOrNull ?: PlayerData() // careful not to modify directly here
                DepositCurrencyOption(data, Modifier.offset(1.dp, 1.dp).clickable {
                    nav.navigate(BankScreen.Deposit)
                })
                WithdrawCurrencyOption(data, Modifier.offset(5.dp, 1.dp).clickable {
                    nav.navigate(BankScreen.Widthdraw)
                })
            }
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
        modifier = modifier.size(3.dp, 2.dp)
    )
}

@Composable
fun WithdrawCurrencyOption(data: PlayerData, modifier: Modifier = Modifier) = Button {
    Text(
        "<gold><b>Open Withdrawal Menu".miniMsg(),
        "<yellow>You currently have <i>${data.orthCoinsHeld} <yellow>coins in your account.".miniMsg(),
        modifier = modifier.size(3.dp, 2.dp)
    )
}
