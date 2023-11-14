package com.mineinabyss.components.lootcrates

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
sealed class LootEntry(
    val weight: Int = 0,
    override val functions: List<LootFunction> = emptyList(),
    override val conditions: List<LootCondition> = emptyList()
): Selecting, Conditioned {
    @Serializable
    @SerialName("minecraft:item")
    data class Item(
        val name: String,
    ) : LootEntry() {
        override fun selectBaseline() = Material.matchMaterial(name)?.let { ItemStack(it) }
    }

    @Serializable
    @SerialName("mineinabyss:item")
    data class IdofrontItem(
        val item: SerializableItemStack,
    ) : LootEntry() {
        override fun selectBaseline() = item.toItemStack()
    }

    @Serializable
    @SerialName("minecraft:group")
    data class Group(
        val children: List<LootEntry>,
    ) : LootEntry() {
        override fun selectBaseline() = children.randomOrNull()?.selectBaseline()
    }
}
