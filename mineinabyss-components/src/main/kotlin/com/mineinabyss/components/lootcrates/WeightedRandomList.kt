package com.mineinabyss.components.lootcrates

class WeightedRandomList<T>(
    val items: List<T>,
    val weight: (T) -> Int,
) {
    private val cumulativeWeights: List<Int> = items
        .runningFold(0) { acc, item -> acc + weight(item) }
        .drop(1)

    private val weightsSum = cumulativeWeights.lastOrNull() ?: 0

    fun chooseRandom(): T? {
        if (weightsSum <= 0) return null

        val random = (1..weightsSum).random()
        return items[cumulativeWeights.indexOfFirst { it >= random }]
    }

    fun chooseRandom(itemCondition: (T) -> Boolean): T? =
        WeightedRandomList(items.filter(itemCondition), weight).chooseRandom()
}
