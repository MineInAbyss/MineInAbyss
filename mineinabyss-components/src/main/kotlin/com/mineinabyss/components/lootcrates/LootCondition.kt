package com.mineinabyss.components.lootcrates

import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class LootCondition {
    abstract fun check(): Boolean

    @Serializable
    @ExcludeAutoScan
    @SerialName("minecraft:random_chance")
    data class RandomChance(val chance: Double) : LootCondition() {
        override fun check() = Math.random() < chance
    }
}
