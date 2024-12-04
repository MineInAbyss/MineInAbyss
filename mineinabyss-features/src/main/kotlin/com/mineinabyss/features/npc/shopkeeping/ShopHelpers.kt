package com.mineinabyss.features.npc.shopkeeping

import androidx.compose.runtime.Composable
import com.mineinabyss.components.npc.shopkeeping.*
import com.mineinabyss.components.playerData
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.CoinFactory
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.Serializable
import net.luckperms.api.node.Node
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShopKeeperQuery(world: Geary) : GearyQuery(world) {
    val key by GearyMobPrefabQuery(world).get<PrefabKey>()
    override fun ensure() = this {
        has<EntityType>()
        has<Prefab>()
        has<ShopKeeper>()
    }
}

object ShopKeepers {
    val query = abyss.gearyGlobal.cache(::ShopKeeperQuery)

    fun getKeys(): List<PrefabKey> = query.mapWithEntity { it.key }.map { it.data }
}


fun getShopTradeCoin(type: ShopCurrency, stack: ItemStack?, cost: Int): ItemStack? {
    return when (type) {
        ShopCurrency.ORTH_COIN -> CoinFactory.orthCoin?.asQuantity(cost)
        ShopCurrency.MITTY_TOKEN -> CoinFactory.mittyToken?.asQuantity(cost)
        ShopCurrency.ITEM -> stack?.asQuantity(cost)
    }
}

fun Player.getShopTradeCost(currencyType: ShopCurrency, currencyStack: ItemStack?): Int {
    return when (currencyType) {
        ShopCurrency.ORTH_COIN -> playerData.orthCoinsHeld
        ShopCurrency.MITTY_TOKEN -> playerData.mittyTokensHeld
        ShopCurrency.ITEM -> getSimilarItemAmount(currencyStack)
    }
}

fun Player.getFirstSimilarItem(item: ItemStack?) = inventory.contents.firstOrNull { i -> i?.isSimilar(item) ?: false }
fun Player.getSimilarItems(item: ItemStack?) = inventory.contents.filter { i -> i != null && i.isSimilar(item) }
fun Player.getSimilarItemAmount(item: ItemStack?) = getSimilarItems(item).sumOf { it?.amount ?: 0 }
fun Player.adjustItemStackAmountFromCost(item: ItemStack?, c: Int): ItemStack? {
    var cost = c
    if (item == null || getSimilarItemAmount(item) < c) return null
    this.getSimilarItems(item).forEach { stack ->
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
                if (player.getShopTradeCost(currencyType, currencyStack) < cost) {
                    player.error("You don't have enough ${currencyType.name.lowercase()}'s!")
                    player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                    return@Button
                }

                when (currencyType) {
                    ShopCurrency.ORTH_COIN -> data.orthCoinsHeld -= cost
                    ShopCurrency.MITTY_TOKEN -> data.mittyTokensHeld -= cost
                    ShopCurrency.ITEM -> player.adjustItemStackAmountFromCost(currencyStack, cost)
                }

                when (tradeAction.action) {
                    TradeAction.GIVE_ITEM -> {
                        if (player.inventory.firstEmpty() != -1) {
                            player.inventory.addItem(tradeItem)
                            player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f)
                        } else {
                            player.error("Your inventory is full!")
                            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                        }
                    }

                    TradeAction.PLAYER_COMMAND -> {
                        if (player.performCommand(tradeAction.getValue(player))) {
                            player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f)
                        } else {
                            player.error("Failed to execute command!")
                        }
                    }

                    TradeAction.CONSOLE_COMMAND -> abyss.plugin.server.dispatchCommand(
                        abyss.plugin.server.consoleSender,
                        tradeAction.getValue(player)
                    )

                    TradeAction.GRANT_PERMISSION -> {
                        val permission = tradeAction.getValue()
                        if (!player.hasPermission(permission)) {
                            luckPerms.userManager.getUser(player.uniqueId)?.data()
                                ?.add(Node.builder(permission).build())
                            player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f)
                        } else {
                            player.error("You already have this permission!")
                            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                        }
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
