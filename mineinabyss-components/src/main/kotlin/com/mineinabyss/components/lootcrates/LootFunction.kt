package com.mineinabyss.components.lootcrates

import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
sealed class LootFunction(
    override val conditions: List<LootCondition> = emptyList(),
): Conditioned {
    abstract fun apply(itemStack: ItemStack)

    @Serializable
    @ExcludeAutoScan
    @SerialName("minecraft:set_count")
    data class SetCount(
        val count: Roll,
    ) : LootFunction() {
        override fun apply(itemStack: ItemStack) {
            itemStack.amount = count.roll()
        }
    }
}
