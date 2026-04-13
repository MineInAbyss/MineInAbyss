package com.mineinabyss.components.lootcrates

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:contains_loot")
class ContainsLoot(
    val table: PrefabKey,
) {
    fun isCustomLoot() = table == LootCrateConstants.CUSTOM_LOOT_TABLE
}

