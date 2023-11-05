package com.mineinabyss.components.lootcrates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.inventory.ItemStack

@Serializable
sealed class LootFunction(
    val conditions: List<LootCondition> = emptyList(),
) {
    abstract fun apply(itemStack: ItemStack)

    @Serializable
    @SerialName("minecraft:set_count")
    data class SetCount(
        val count: Roll,
    ) : LootFunction() {
        override fun apply(itemStack: ItemStack) {
            itemStack.amount = count.roll()
        }
    }
}
