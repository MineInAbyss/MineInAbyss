package com.mineinabyss.npc.orthbanking.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.Text
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.npc.orthbanking.depositCoins
import org.bukkit.entity.Player

@Composable
fun DepositScreen(player: Player) {
    var amount = 1

    Button(
        Modifier.at(3, 0),
        onClick = {
            amount += 1
            if (amount > 64) amount = 64
        }
    ) {
        Text("<gold><b>Increase Deposit".miniMsg(), modifier = Modifier.size(3, 2))
    }

    Button(
        Modifier.at(4, 2),
        onClick = {
            player.depositCoins(amount)
            player.closeInventory()
        }
    ) {
        Text("<gold><b>Confirm Deposit".miniMsg())
    }

    Button(
        Modifier.at(3, 3),
        onClick = {
            amount -= 1
            if (amount < 1) amount = 1
        }
    ) {
        Text("<gold><b>Decrease Deposit".miniMsg(), modifier = Modifier.size(3, 1))
    }
}
