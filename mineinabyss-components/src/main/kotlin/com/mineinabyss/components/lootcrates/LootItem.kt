package com.mineinabyss.components.lootcrates

import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable

@Serializable
class LootItem(
    val item: SerializableItemStack,
    @Serializable(with = IntRangeSerializer::class)
    val amount: IntRange,
)
