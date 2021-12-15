package com.mineinabyss.components.curse

import org.bukkit.entity.Player
import kotlin.time.Duration

interface AscensionEffect {
    fun applyEffect(player: Player, ticks: Int)
    val isDone: Boolean
    val duration: Duration
    fun cleanUp(player: Player)

    fun clone(): AscensionEffect
}
