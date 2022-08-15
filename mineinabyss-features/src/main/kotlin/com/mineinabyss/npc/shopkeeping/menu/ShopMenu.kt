package com.mineinabyss.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.ShopMenu(player: Player, shopKeeper: ShopKeeper) {
    val shopKeeperName = shopKeeper.name.miniMsg()

    Chest(setOf(player),
        shopKeeperName,
        Modifier.height(6),
        onClose = { player.closeInventory() }) {
        PlayerBalance(player, Modifier.at(8, 0))
        ShopKeeperTrades(player, shopKeeper, Modifier.at(0,1))
    }
}

fun PlayerBalance(player: Player, modifier: Modifier) {
    val amount = player.playerData.orthCoinsHeld
    Item(TitleItem.of("<#FFBB1C>${amount} <b>Orth Coin${if (amount != 1) "s" else ""}".miniMsg()), modifier)
}

fun ShopKeeperTrades(player: Player, shopKeeper: ShopKeeper, modifier: Modifier) {
    Grid(modifier.size(4, 5)) {
        shopKeeper.trades.forEach { (price, item) ->
            val cost = price.amount ?: return@forEach
            val slot = player.inventory.firstEmpty()
            Button(
                enabled = player.playerData.orthCoinsHeld >= cost && slot != -1,
                onClick = {
                    player.playerData.orthCoinsHeld -= cost
                    player.inventory.setItem(slot, item.toItemStack())
                }
            ) {
                Item(price.toItemStack())
                Spacer(width = 2)
                Item(item.toItemStack())
            }
        }
    }
}
