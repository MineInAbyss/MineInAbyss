package com.mineinabyss.features.orthbanking.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.features.helpers.Text
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.features.orthbanking.withdrawCoins
import com.mineinabyss.guiy.canvas.LocalGuiyOwner
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.idofront.textcomponents.miniMsg
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.modifier.Modifier
import org.bukkit.entity.Player

@Composable
fun WithdrawScreen(player: Player) = Chest(":space_-8::orthbanker_withdrawal_menu:", Modifier.height(5.dp)) {
    var amount = 1
    val owner = LocalGuiyOwner.current

    Button(
        Modifier.offset(3.dp, 0.dp),
        onClick = {
            amount += 1
            if (amount > 64) amount = 64
        }
    ) {
        Text("<gold><b>Increase Withdrawal".miniMsg(), modifier = Modifier.size(3.dp, 2.dp))
    }

    Button(
        Modifier.offset(4.dp, 2.dp),
        onClick = {
            player.withdrawCoins(amount)
            owner.exit()
        }
    ) {
        Text("<gold><b>Confirm Withdrawal".miniMsg())
    }

    Button(
        Modifier.offset(3.dp, 3.dp),
        onClick = {
            amount -= 1
            if (amount < 1) amount = 1
        }
    ) {
        Text("<gold><b>Decrease Withdrawal".miniMsg(), modifier = Modifier.size(3.dp, 1.dp))
    }
}
