package com.mineinabyss.components.whistle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Whistle {
    BELL, RED, BLUE, MOON, BLACK, WHITE;

    val id: String get() = name.lowercase()
}

@Serializable
@SerialName("mineinabyss:player_whistle")
data class PlayerWhistle(val rank: Whistle)
