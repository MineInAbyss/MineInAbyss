package com.mineinabyss.npc.shopkeeping

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.shopkeeping.*
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.helpers.CoinFactory
import com.mineinabyss.helpers.luckPerms
import com.mineinabyss.helpers.ui.composables.Button
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.mineinabyss.mobzy.ecs.components.initialization.MobzyType
import com.mineinabyss.mobzy.injection.MobzyTypesQuery
import kotlinx.serialization.Serializable
import net.luckperms.api.node.Node
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ShopKeeperQuery : GearyQuery() {
    val TargetScope.key by MobzyTypesQuery.get<PrefabKey>()
    val TargetScope.isShopkeeper by family {
        has<MobzyType>()
        has<Prefab>()
        has<ShopKeeper>()
    }

    fun getKeys(): List<PrefabKey> = ShopKeeperQuery.run { map { it.key } }
}

fun getShopTradeCoin(type: ShopCurrency, stack: ItemStack?, cost: Int): ItemStack? {
    return when (type) {
        ShopCurrency.ORTH_COIN -> CoinFactory.orthCoin?.asQuantity(cost)
        ShopCurrency.MITTY_TOKEN -> CoinFactory.mittyToken?.asQuantity(cost)
        ShopCurrency.ITEM -> stack?.asQuantity(cost)
    }
}

fun Player.getShopTradeCost(currencyType: ShopCurrency, currencyStack: ItemStack?) : Int {
    return when (currencyType) {
        ShopCurrency.ORTH_COIN -> playerData.orthCoinsHeld
        ShopCurrency.MITTY_TOKEN -> playerData.mittyTokensHeld
        ShopCurrency.ITEM -> getSimilarItemAmount(currencyStack)
    }
}

fun Player.getFirstSimilarItem(item: ItemStack?) = inventory.contents?.firstOrNull { i -> i?.isSimilar(item) ?: false }
fun Player.getSimilarItems(item: ItemStack?) = inventory.contents?.filter { i -> i != null && i.isSimilar(item) }
fun Player.getSimilarItemAmount(item: ItemStack?) = getSimilarItems(item)?.sumOf { it?.amount ?: 0 } ?: 0
fun Player.adjustItemStackAmountFromCost(item: ItemStack?, c: Int) : ItemStack? {
    var cost = c
    if (item == null || getSimilarItemAmount(item) < c) return null
    this.getSimilarItems(item)?.forEach { stack ->
        if (stack == null) return@forEach

        val amount = stack.amount
        if (amount > cost) {
            stack.subtract(cost)
            return stack
        } else {
            cost -= amount
            stack.subtract(amount)
        }
    }
    //This should never be reached as we verify
    //the player has the [cost] amount of items in their inventory
    return null
}

@Composable
fun List<@Serializable(with = ShopTradeSerializer::class) ShopTrade>.handleTrades(player: Player) {
    val data = player.playerData
    this.forEach { (item, currency, currencyType, cost, tradeAction) ->
        val tradeItem = item.toItemStack()
        val currencyStack = currency?.toItemStack() ?: if (currencyType == ShopCurrency.ITEM) return@forEach else null
        val coin = getShopTradeCoin(currencyType, currencyStack, cost) ?: return@forEach

        Button(
            onClick = {
                if (player.getShopTradeCost(currencyType, currencyStack) < cost) return@Button

                when (tradeAction.action) {
                    TradeAction.GIVE_ITEM -> {
                        if (player.inventory.firstEmpty() == -1) return@Button
                        player.inventory.addItem(tradeItem)
                    }
                    TradeAction.PLAYER_COMMAND -> player.performCommand(tradeAction.getValue(player))
                    TradeAction.CONSOLE_COMMAND -> mineInAbyss.server.dispatchCommand(mineInAbyss.server.consoleSender, tradeAction.getValue(player))
                    TradeAction.GRANT_PERMISSION -> {
                        val permission = tradeAction.getValue()
                        if (player.hasPermission(permission)) return@Button
                        luckPerms.userManager.getUser(player.uniqueId)?.data()?.add(Node.builder(permission).build())
                    }
                }

                when (currencyType) {
                    ShopCurrency.ORTH_COIN -> data.orthCoinsHeld -= cost
                    ShopCurrency.MITTY_TOKEN -> data.mittyTokensHeld -= cost
                    ShopCurrency.ITEM -> player.adjustItemStackAmountFromCost(currencyStack, cost) ?: return@Button
                }
            },
        ) {
            //TODO Alter how disabled trades are displayed
            Item(coin)
            Spacer(width = 1)
            Item(tradeItem)
        }
    }
}
