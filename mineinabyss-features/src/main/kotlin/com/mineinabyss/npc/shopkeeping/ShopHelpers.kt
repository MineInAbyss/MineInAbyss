package com.mineinabyss.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopCurrency
import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.helpers.createMittyToken
import com.mineinabyss.helpers.createOrthCoin
import com.mineinabyss.mobzy.ecs.components.initialization.MobzyType
import com.mineinabyss.mobzy.injection.MobzyTypesQuery
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
        ShopCurrency.ORTH_COIN -> createOrthCoin()?.asQuantity(cost)
        ShopCurrency.MITTY_TOKEN -> createMittyToken()?.asQuantity(cost)
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
