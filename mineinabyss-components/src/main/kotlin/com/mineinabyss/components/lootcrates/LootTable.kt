package com.mineinabyss.components.lootcrates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("mineinabyss:loot_table")
class LootTable(
    val pools: List<LootPool>
) {
    fun select(): List<ItemStack?> {
        return pools
            .filter { it.conditionsMet() }
            .flatMap { pool ->
                (1..(pool.rolls.roll())).map { pool.select() }
            }
    }

    fun populateInventory(inventory: Inventory) {
        val selected = select().take(inventory.size)
        val selectedWithAirGaps = (selected + List(inventory.size - selected.size) { null }).shuffled()
        selectedWithAirGaps.forEachIndexed { index, itemStack ->
            inventory.setItem(index, itemStack)
        }
    }

    companion object {
        fun empty() = LootTable(emptyList())
    }
}

