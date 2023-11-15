package com.mineinabyss.components.lootcrates

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.inventory.ItemStack

@Serializable
class LootPool(
    val rolls: Roll = Roll.Constant(1),
    val entries: List<LootEntry>,
    override val functions: List<LootFunction> = emptyList(),
    override val conditions: List<LootCondition> = emptyList(),
): Selecting, Conditioned {
    @Transient
    val entryHolder = LootEntryHolder(entries)

    override fun selectBaseline(): ItemStack? {
        return entryHolder.selectFromRandomEntry()
    }
}
