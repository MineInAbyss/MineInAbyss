package com.mineinabyss.components.curse

import com.mineinabyss.idofront.time.TimeSpan
import org.bukkit.entity.Player

interface AscensionEffect {
    fun applyEffect(player: Player, ticks: Int)
    val isDone: Boolean
    val duration: TimeSpan
    fun cleanUp(player: Player)

    fun clone(): AscensionEffect
}
