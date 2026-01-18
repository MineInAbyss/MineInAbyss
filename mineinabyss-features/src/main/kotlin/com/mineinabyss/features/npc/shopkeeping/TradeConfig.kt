package com.mineinabyss.features.npc.shopkeeping

import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit.getWorld
import org.bukkit.inventory.MerchantRecipe

@Serializable
data class TradeEntry(
    val prefab: String,
    @EncodeDefault(NEVER) val amount: Int = 1,
)

@Serializable
data class Trade(
    val input: TradeEntry,
    val output: TradeEntry,
)

@Serializable
class TradeTable(
    val id: String,
    @EncodeDefault(NEVER) val trades: List<Trade> = emptyList(),
) {
    fun createMerchantRecipes(): List<MerchantRecipe>? {
        val gearyItems = getWorld("world")?.toGeary()?.getAddon(ItemTracking) ?: return null

        return trades.map { trade ->
            val inputItem = gearyItems.createItem(PrefabKey.of(trade.input.prefab)) ?: error("Incorrect prefab key: ${trade.input.prefab}")
            inputItem.amount = trade.input.amount
            val outputItem = gearyItems.createItem(PrefabKey.of(trade.output.prefab)) ?: error("Incorrect prefab key: ${trade.output.prefab}")
            outputItem.amount = trade.output.amount

            MerchantRecipe(outputItem, 99999).apply { addIngredient(inputItem) }
        }
    }
}
