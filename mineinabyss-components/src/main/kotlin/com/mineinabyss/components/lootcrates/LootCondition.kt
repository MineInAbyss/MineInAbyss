package com.mineinabyss.components.lootcrates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class LootCondition {
    abstract fun check(): Boolean

    @Serializable
    @SerialName("minecraft:random_chance")
    data class RandomChance(val chance: Double) : LootCondition() {
        override fun check() = Math.random() < chance
    }
}
