package com.mineinabyss.components.npc.shopkeeping


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO Make [name] be reflected as a nametag on the entity
@Serializable
@SerialName("mineinabyss:shopkeeper")
class ShopKeeper(
    val name: String = "",
    val menu: String = "",
    val trades: List<@Serializable(with = ShopTradeSerializer::class) ShopTrade> = emptyList()
)
