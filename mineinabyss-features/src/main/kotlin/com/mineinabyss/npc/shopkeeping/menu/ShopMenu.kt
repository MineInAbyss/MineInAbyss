package com.mineinabyss.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.shopkeeping.ShopCurrency
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.components.Grid
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.canvases.MAX_CHEST_HEIGHT
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.helpers.TitleItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.idofront.messaging.miniMsg
import com.mineinabyss.npc.shopkeeping.adjustItemStackAmountFromCost
import com.mineinabyss.npc.shopkeeping.getShopTradeCoin
import com.mineinabyss.npc.shopkeeping.getShopTradeCost
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.ShopMenu(player: Player, shopKeeper: ShopKeeper) {
    Chest(setOf(player),
        shopKeeper.menu.miniMsg().append(shopKeeper.name.miniMsg()),
        Modifier.height(MAX_CHEST_HEIGHT),
        onClose = { player.closeInventory() }) {
        //PlayerBalance(player, Modifier.at(8, 0))
        ShopKeeperTrades(player, shopKeeper, Modifier.at(3, 0))
    }
}

@Composable
fun PlayerBalance(player: Player, modifier: Modifier) {
    val amount = player.playerData.orthCoinsHeld
    Item(TitleItem.of("<#FFBB1C>${amount} <b>Orth Coin${if (amount != 1) "s" else ""}".miniMsg()), modifier)
}

@Composable
fun ShopKeeperTrades(player: Player, shopKeeper: ShopKeeper, modifier: Modifier) {
    Grid(modifier.size(5, 6)) {
        val data = player.playerData
        shopKeeper.trades.forEach { (item, currency, currencyType, cost) ->
            val tradeItem = item.toItemStack()
            val currencyStack = currency?.toItemStack() ?: if (currencyType == ShopCurrency.ITEM) return@forEach else null
            val coin = getShopTradeCoin(currencyType, currencyStack, cost) ?: return@forEach

            Button(
                onClick = {
                    if (player.getShopTradeCost(currencyType, currencyStack) < cost || player.inventory.firstEmpty() == -1)
                        return@Button
                    when (currencyType) {
                        ShopCurrency.ORTH_COIN -> data.orthCoinsHeld -= cost
                        ShopCurrency.MITTY_TOKEN -> data.mittyTokensHeld -= cost
                        ShopCurrency.ITEM -> player.adjustItemStackAmountFromCost(currencyStack, cost) ?: return@Button
                    }
                    player.updateBalance()
                    player.inventory.addItem(tradeItem)
                },
            ) {
                //TODO Alter how disabled trades are displayed
                Item(coin)
                Spacer(width = 1)
                Item(tradeItem)
            }
        }
    }
}
