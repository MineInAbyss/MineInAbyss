package com.mineinabyss.components.lootcrates

interface Conditioned {
    val conditions: List<LootCondition>

    fun conditionsMet() = conditions.all { it.check() }
}
