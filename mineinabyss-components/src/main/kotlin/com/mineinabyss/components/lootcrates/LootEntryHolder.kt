package com.mineinabyss.components.lootcrates

import org.bukkit.inventory.ItemStack

class LootEntryHolder(
    entries: List<LootEntry>
) {
    val weightedRandomList = WeightedRandomList(entries) { it.weight }
    fun selectFromRandomEntry(): ItemStack? {
        return weightedRandomList.chooseRandom { it.conditionsMet() }.select()
    }
}
