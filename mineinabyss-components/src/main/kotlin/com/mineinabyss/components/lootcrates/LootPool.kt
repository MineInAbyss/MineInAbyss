package com.mineinabyss.components.lootcrates

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.inventory.ItemStack

@Serializable
class LootPool(
    val entries: List<LootSelector>,
    val weight: Int = 1,
    val functions: List<LootFunction> = emptyList(),
) {
    @Transient
    val weightedEntries = WeightedRandomList(entries) { it.weight }

    fun select(): ItemStack? {
        return weightedEntries.chooseRandom().select()?.apply {
            functions
                .filter { function -> function.conditions.all { it.check() } }
                .forEach { it.apply(this) }
        }
    }
}

