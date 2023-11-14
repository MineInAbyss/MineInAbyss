package com.mineinabyss.components.lootcrates

import org.bukkit.inventory.ItemStack

interface Selecting {
    val functions: List<LootFunction>

    fun select(): ItemStack? {
        return selectBaseline()?.apply {
            functions
                .filter { it.conditionsMet() }
                .forEach { it.apply(this) }
        }
    }

    fun selectBaseline(): ItemStack?
}
