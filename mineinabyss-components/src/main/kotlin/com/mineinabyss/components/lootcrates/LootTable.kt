package com.mineinabyss.components.lootcrates

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("mineinabyss:loot_table")
class LootTable(
    val rolls: Roll,
    val pools: List<LootPool>
) {
    @Transient
    private val cumulativeWeights: List<Int> = pools
        .runningFold(0) { acc, lootPool -> acc + lootPool.weight }
        .drop(1)

    @Transient
    private val weightsSum = cumulativeWeights.last()

    fun select(): List<ItemStack?> {
        val items = mutableListOf<ItemStack?>()
        repeat(rolls.roll()) {
            val random = (0..weightsSum).random()
            items.add(pools[cumulativeWeights.indexOfFirst { it >= random }].select())
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
}

@Serializable
class LootPool(
    val entries: List<LootSelector>,
    val weight: Int = 1,
) {
    fun select(): ItemStack? {
        return entries.random().select()
    }
}

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

@Serializable
sealed class Roll {
    abstract fun roll(): Int

    @Serializable
    @SerialName("minecraft:uniform")
    data class Uniform(val min: Int, val max: Int) : Roll() {
        override fun roll(): Int {
            return (min..max).random()
        }
    }

    @Serializable
    @SerialName("minecraft:constant")
    data class Constant(val value: Int) : Roll() {
        override fun roll() = value
    }

    @Serializable
    @SerialName("minecraft:binomial")
    data class Binomial(val n: Int, val p: Double) : Roll() {
        override fun roll(): Int {
            var successes = 0
            repeat(n) {
                if (Math.random() < p) successes++
            }
            return successes
        }
    }
}

@Serializable
sealed class LootCondition {
    abstract fun check(): Boolean

    @Serializable
    @SerialName("minecraft:random_chance")
    data class RandomChance(val chance: Double) : LootCondition() {
        override fun check() = Math.random() < chance
    }
}
