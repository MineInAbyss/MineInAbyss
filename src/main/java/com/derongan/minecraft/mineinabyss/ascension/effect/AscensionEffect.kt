package com.derongan.minecraft.mineinabyss.ascension.effect

import org.bukkit.entity.Player

interface AscensionEffect {
    fun applyEffect(player: Player, ticks: Int)
    val isDone: Boolean
    val duration: Int
    fun cleanUp(player: Player)

    fun clone(): AscensionEffect
}