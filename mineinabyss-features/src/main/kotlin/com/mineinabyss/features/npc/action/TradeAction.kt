package com.mineinabyss.features.npc.action

import kotlinx.serialization.Serializable

class TradeAction {
}


@Serializable
data class TradeEntry(
    val prefab: String,
    val amount: Int = 1,
)

@Serializable
data class Trade(
    val input: TradeEntry,
    val output: TradeEntry,
)

@Serializable
class TradeTable(
    val id: String,
    val trades: List<Trade>,
)
