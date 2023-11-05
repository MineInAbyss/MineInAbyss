package com.mineinabyss.components.lootcrates

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
sealed class LootSelector {
    val weight: Int = 0
    val conditions: List<LootCondition> = emptyList()
    abstract fun select(): ItemStack?

    @Serializable
    @SerialName("minecraft:item")
    data class Item(
        val name: String,
    ) : LootSelector() {
        override fun select() = Material.matchMaterial(name)?.let { ItemStack(it) }
    }

    @Serializable
    @SerialName("mineinabyss:item")
    data class IdofrontItem(
        val item: SerializableItemStack,
    ) : LootSelector() {
        override fun select() = item.toItemStack()
    }
}
