package com.mineinabyss.features.goals

import kotlinx.serialization.Serializable

// for stuff like "relics", "bonfire ingredients" and whatnot
@Serializable
data class ItemCategory(val categoryName: String, val items: List<String>)

class ItemCategories(categories: List<ItemCategory>) {
    private val byItem: Map<String, List<String>> =
        buildMap<String, MutableList<String>> {
            categories.forEach { category ->
                category.items.forEach { getOrPut(it) { mutableListOf() }.add(category.categoryName) }
            }
        }

    fun categoriesOf(factIds: Collection<String>): Set<String> =
        factIds.flatMapTo(mutableSetOf()) { byItem[it].orEmpty() }
}
