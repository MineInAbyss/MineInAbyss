package com.mineinabyss.components.lootcrates

import kotlinx.serialization.Transient

class WeightedRandomList<T>(
    val items: List<T>,
    val weight: (T) -> Int,
) {
    @Transient
    private val cumulativeWeights: List<Int> = items
        .runningFold(0) { acc, item -> acc + weight(item) }
        .drop(1)

    @Transient
    private val weightsSum = cumulativeWeights.lastOrNull() ?: 0

    fun chooseRandom(): T {
        val random = (0..weightsSum).random()
        return items[cumulativeWeights.indexOfFirst { it >= random }]
    }

    fun chooseRandom(itemCondition: (T) -> Boolean): T {
        return WeightedRandomList(items.filter(itemCondition), weight).chooseRandom()
    }
}
