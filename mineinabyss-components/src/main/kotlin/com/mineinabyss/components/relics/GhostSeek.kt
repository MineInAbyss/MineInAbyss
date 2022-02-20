package com.mineinabyss.components.relics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.inventivetalent.glow.GlowAPI

//TODO Try and get hex-colors to work again

@Serializable
@SerialName("mineinabyss:ghost_seek")
data class GhostSeek(
    val distance: Double = 100.0,
    val playerColor: GlowAPI.Color = GlowAPI.Color.DARK_GREEN,
    val passiveMobColor: GlowAPI.Color = GlowAPI.Color.GREEN,
    val hostileMobColor: GlowAPI.Color = GlowAPI.Color.RED,
    val flyingMobColor: GlowAPI.Color = GlowAPI.Color.YELLOW,
    val waterMobColor: GlowAPI.Color = GlowAPI.Color.DARK_BLUE,
    val bossMob: GlowAPI.Color = GlowAPI.Color.DARK_PURPLE
)