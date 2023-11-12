package com.mineinabyss.components.lootcrates

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:contains_loot")
class ContainsLoot(
    val table: String
) {
    fun isCustomLoot() = table == LootCrateContants.CUSTOM_LOOT_TABLE
}
