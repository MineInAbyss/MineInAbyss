package com.mineinabyss.npc.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.components.PlayerData
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.*
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.helpers.ui.rememberNavigation
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.idofront.font.Space
import org.bukkit.ChatColor.*
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
fun DepositCurrencyOption(data: PlayerData, modifier: Modifier = Modifier) = Button(
    TitleItem.of(
        "$GOLD${BOLD}Open Deposit Menu",
        "${YELLOW}You currently have $ITALIC${data.orthCoinsHeld} ${YELLOW}coins in your account."
    ),
    modifier.size(3, 2)
)

@Composable
fun WithdrawCurrencyOption(data: PlayerData, modifier: Modifier = Modifier) = Button(
    TitleItem.of(
        "$GOLD${BOLD}Open Withdrawal Menu",
        "${YELLOW}You currently have $ITALIC${data.orthCoinsHeld} ${YELLOW}coins in your account."
    ),
    modifier.size(3, 2)
)

