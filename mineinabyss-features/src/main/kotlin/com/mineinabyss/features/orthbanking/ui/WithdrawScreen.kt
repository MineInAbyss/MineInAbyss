package com.mineinabyss.features.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.features.orthbanking.withdrawCoins
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun WithdrawScreen(player: Player) {
    var amount = 1

    Button(
        Modifier.at(3, 0),
        onClick = {
            amount += 1
            if (amount > 64) amount = 64
        }
    ) {
        Text("<gold><b>Increase Withdrawal".miniMsg(), modifier = Modifier.size(3, 2))
    }

    Button(
        Modifier.at(4, 2),
        onClick = {
            player.withdrawCoins(amount)
            player.closeInventory()
        }
    ) {
        Text("<gold><b>Confirm Withdrawal".miniMsg())
    }

    Button(
        Modifier.at(3, 3),
        onClick = {
            amount -= 1
            if (amount < 1) amount = 1
        }
    ) {
        Text("<gold><b>Decrease Withdrawal".miniMsg(), modifier = Modifier.size(3, 1))
    }
}
