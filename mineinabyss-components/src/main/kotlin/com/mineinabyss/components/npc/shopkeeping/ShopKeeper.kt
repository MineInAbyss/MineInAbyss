package com.mineinabyss.components.npc.shopkeeping


import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:shopkeeper")
class ShopKeeper(
    val name: String = "",
    val trades: Map<SerializableItemStack, SerializableItemStack> = emptyMap()
)
