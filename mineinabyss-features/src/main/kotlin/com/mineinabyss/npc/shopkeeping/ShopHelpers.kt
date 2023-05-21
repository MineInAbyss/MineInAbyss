package com.mineinabyss.npc.shopkeeping

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.shopkeeping.ShopCurrency
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.components.npc.shopkeeping.ShopTrade
import com.mineinabyss.components.npc.shopkeeping.ShopTradeSerializer
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.helpers.CoinFactory
import com.mineinabyss.helpers.ui.composables.Button
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ShopKeeperQuery : GearyQuery() {
    val TargetScope.key by GearyMobPrefabQuery().get<PrefabKey>()
    val TargetScope.isShopkeeper by family {
        has<EntityType>()
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
    this.forEach { (item, currency, currencyType, cost) ->
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
