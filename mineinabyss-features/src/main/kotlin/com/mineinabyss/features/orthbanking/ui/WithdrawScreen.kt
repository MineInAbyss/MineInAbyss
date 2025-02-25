package com.mineinabyss.features.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.features.orthbanking.withdrawCoins
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.placement.absolute.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player

@Composable
fun WithdrawScreen(player: Player) {
    var amount = 1

    Button(
        onClick = {
            amount += 1
            if (amount > 64) amount = 64
        },
        Modifier.at(3, 0),
    ) {
        Text("<gold><b>Increase Withdrawal", modifier = Modifier.size(3, 2))
    }

    Button(
        onClick = {
            player.withdrawCoins(amount)
            player.closeInventory()
        },
        Modifier.at(4, 2),
    ) {
        Text("<gold><b>Confirm Withdrawal")
    }

    Button(
        onClick = {
            amount -= 1
            if (amount < 1) amount = 1
        },
        Modifier.at(3, 3),
    ) {
        Text("<gold><b>Decrease Withdrawal", modifier = Modifier.size(3, 1))
    }
}
