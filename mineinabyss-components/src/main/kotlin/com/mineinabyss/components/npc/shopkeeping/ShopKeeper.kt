package com.mineinabyss.components.npc.shopkeeping


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:shopkeeper")
class ShopKeeper(
    val name: String = "",
    val trades: List<@Serializable(with = ShopTradeSerializer::class) ShopTrade> = emptyList()
)
