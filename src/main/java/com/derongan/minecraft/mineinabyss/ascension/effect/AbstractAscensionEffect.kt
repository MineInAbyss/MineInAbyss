package com.derongan.minecraft.mineinabyss.ascension.effect

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.idofront.time.TimeSpan
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.entity.Player

abstract class AbstractAscensionEffect : AscensionEffect {
    abstract val offset: TimeSpan
    abstract val iterations: Int

    override fun applyEffect(player: Player, ticks: Int) {
        mineInAbyss.schedule {
            waitFor(offset.ticks)
            repeat(iterations) {
                applyEffect(player)
                waitFor(duration.ticks)
            }
            cleanUp(Bukkit.getPlayer(player.uniqueId) ?: player) //get new player reference if player relogged
        }
    }

    abstract fun applyEffect(player: Player)

    override val isDone: Boolean
        get() = duration.millis <= 0

    override fun cleanUp(player: Player) {}
}