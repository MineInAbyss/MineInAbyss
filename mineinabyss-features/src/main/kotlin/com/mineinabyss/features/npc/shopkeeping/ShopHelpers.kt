package com.mineinabyss.features.npc.shopkeeping

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.components.npc.shopkeeping.*
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.CoinFactory
import com.mineinabyss.features.helpers.adjustItemStackAmountFromCost
import com.mineinabyss.features.helpers.luckPerms
import com.mineinabyss.features.helpers.similarItemAmount
import com.mineinabyss.features.helpers.ui.composables.Button
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.entities.helpers.GearyMobPrefabQuery
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.Spacer
import com.mineinabyss.idofront.messaging.error
import kotlinx.serialization.Serializable
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier
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


fun shopTradeCoin(type: ShopCurrency, stack: ItemStack?, cost: Int): ItemStack? {
    return when (type) {
        ShopCurrency.ORTH_COIN -> CoinFactory.orthCoin?.asQuantity(cost)
        ShopCurrency.MITTY_TOKEN -> CoinFactory.mittyToken?.asQuantity(cost)
        ShopCurrency.ITEM -> stack?.asQuantity(cost)
    }
}

fun Player.shopTradeCost(currencyType: ShopCurrency, currencyStack: ItemStack?): Int {
    return when (currencyType) {
        ShopCurrency.ORTH_COIN -> playerDataOrNull?.orthCoinsHeld ?: 0
        ShopCurrency.MITTY_TOKEN -> playerDataOrNull?.mittyTokensHeld ?: 0
        ShopCurrency.ITEM -> similarItemAmount(currencyStack)
    }
}

@Composable
fun List<@Serializable(with = ShopTradeSerializer::class) ShopTrade>.handleTrades(player: Player) {
    val data = player.playerDataOrNull ?: return
    this.forEach { (item, currency, currencyType, cost, tradeAction) ->
        val tradeItem = item.toItemStack()
        val currencyStack = currency?.toItemStack() ?: if (currencyType == ShopCurrency.ITEM) return@forEach else null
        val coin = shopTradeCoin(currencyType, currencyStack, cost) ?: return@forEach

        Button(
            onClick = {
                if (player.shopTradeCost(currencyType, currencyStack) < cost) {
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
                        if (player.performCommand(tradeAction.getValue(player)))
                            player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1f, 1f)
                        else player.error("Failed to execute command!")
                    }

                    TradeAction.CONSOLE_COMMAND -> abyss.server.dispatchCommand(
                        abyss.server.consoleSender,
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
            },
        ) {
            //TODO Alter how disabled trades are displayed
            Item(coin)
            Spacer(Modifier.width(1.dp))
            Item(tradeItem)
        }
    }
}
