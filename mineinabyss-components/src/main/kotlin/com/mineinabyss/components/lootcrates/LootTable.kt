package com.mineinabyss.components.lootcrates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("mineinabyss:loot_table")
class LootTable(
    val rolls: Roll,
    val pools: List<LootPool>
) {
    @Transient
    val weightedPools = WeightedRandomList(pools) { it.weight }

    fun select(): List<ItemStack?> {
        val items = mutableListOf<ItemStack?>()
        repeat(rolls.roll()) {
            items.add(weightedPools.chooseRandom().select())
        }
        return items
    }

    fun populateInventory(inventory: Inventory) {
        val selected = select().take(inventory.size)
        val selectedWithAirGaps = (selected + List(inventory.size - selected.size) { null }).shuffled()
        selectedWithAirGaps.forEachIndexed { index, itemStack ->
            inventory.setItem(index, itemStack)
        }
    }

    companion object{
        fun empty() = LootTable(Roll.Uniform(0, 0), emptyList())
    }
}

