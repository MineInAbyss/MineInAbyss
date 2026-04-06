package com.mineinabyss.features.npc.shopkeeping

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getWorld
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe

@Serializable
data class TradeEntry(
    val prefab: String,
    val amount: Int = 1,
    @EncodeDefault(NEVER) val components: String? = null,
) {
}

@Serializable
data class Trade(
    val input: TradeEntry,
    val output: TradeEntry,
)

@Serializable
class TradeTable(
    @EncodeDefault(NEVER) val trades: List<Trade> = emptyList(),
) {
    fun createMerchantRecipes(): List<MerchantRecipe>? {
        val gearyItems = getWorld("world")?.toGeary()?.getAddon(ItemTracking) ?: return null

        return trades.map { trade ->
            val inputItem = createItemFromTradeEntry(trade.input)
            inputItem.amount = trade.input.amount
            val outputItem = createItemFromTradeEntry(trade.output)
            outputItem.amount = trade.output.amount

            MerchantRecipe(outputItem, 99999).apply { addIngredient(inputItem) }
        }
    }

    fun createItemFromTradeEntry(entry :TradeEntry): ItemStack {
        val prefabNamespace = entry.prefab.split(":").first();
        if (prefabNamespace == "mineinabyss") {
            val gearyItems = getWorld("world")?.toGeary()?.getAddon(ItemTracking) ?: error("Could not get world or item tracking addon")
            return gearyItems.createItem(PrefabKey.of(entry.prefab))?.asQuantity(entry.amount) ?: error("Incorrect prefab key: ${entry.prefab}")
        } else if (prefabNamespace == "minecraft") {

            val itemFactory = Bukkit.getItemFactory()
            val itemStr = if (entry.components != null) entry.prefab + entry.components else entry.prefab
            val stack = try {
                itemFactory.createItemStack(itemStr)

            } catch (e: IllegalArgumentException) {
                error("Invalid minecraft item string: $itemStr")
            }
            return stack
        } else {
            error("Incorrect prefab namespace: ${entry.prefab}")
        }
    }




}

@Serializable
class TradeTablesConfig(
    @EncodeDefault(NEVER) val tradeTables: Map<String, TradeTable> = mapOf(),
)

object TradeConfigHolder {
    var config: TradeTablesConfig? = null
}

