package com.mineinabyss.npc.shopkeeping.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.shopkeeping.ShopCurrency
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
import com.mineinabyss.helpers.createMittyToken
import com.mineinabyss.helpers.createOrthCoin
import com.mineinabyss.helpers.getFirstSimilarItem
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.idofront.messaging.miniMsg
import org.bukkit.entity.Player

@Composable
fun GuiyOwner.ShopMenu(player: Player, shopKeeper: ShopKeeper) {
    val shopKeeperName = shopKeeper.name.miniMsg()
    val shopMenu = shopKeeper.menu.miniMsg()

    Chest(setOf(player),
        shopMenu.append(shopKeeperName),
        Modifier.height(6),
        onClose = { player.closeInventory() }) {
        PlayerBalance(player, Modifier.at(8, 0))
        ShopKeeperTrades(player, shopKeeper, Modifier.at(0, 1))
    }
}

@Composable
fun PlayerBalance(player: Player, modifier: Modifier) {
    val amount = player.playerData.orthCoinsHeld
    Item(TitleItem.of("<#FFBB1C>${amount} <b>Orth Coin${if (amount != 1) "s" else ""}".miniMsg()), modifier)
}

@Composable
fun ShopKeeperTrades(player: Player, shopKeeper: ShopKeeper, modifier: Modifier) {
    Grid(modifier.size(4, 5)) {
        shopKeeper.trades.forEach { (item, currency, currencyType, cost) ->
            val data = player.playerData
            val slot = player.inventory.firstEmpty()
            val balance = if (currencyType == ShopCurrency.ORTH_COIN) data.orthCoinsHeld else data.mittyTokensHeld
            val coin =
                when (currencyType) {
                    ShopCurrency.ORTH_COIN -> createOrthCoin()
                    ShopCurrency.MITTY_TOKEN -> createMittyToken()
                    ShopCurrency.ITEM -> currency?.toItemStack()
                } ?: return@forEach
            coin.amount = cost
            Button(
                enabled = balance >= cost && slot != -1,
                onClick = {
                    when (currencyType) {
                        ShopCurrency.ORTH_COIN -> data.orthCoinsHeld -= cost
                        ShopCurrency.MITTY_TOKEN -> data.mittyTokensHeld -= cost
                        ShopCurrency.ITEM -> player.getFirstSimilarItem(currency?.toItemStack())?.amount?.minus(cost) ?: return@Button
                    }
                    player.inventory.setItem(slot, item.toItemStack())
                }
            ) {
                //TODO Alter how disabled trades are displayed
                Item(coin)
                Spacer(width = 1)
                Item(item.toItemStack())
            }
        }
    }
}
