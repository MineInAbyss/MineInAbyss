package com.mineinabyss.curse.effects

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.idofront.time.TimeSpan
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Serializable
sealed class AbstractAscensionEffect : AscensionEffect {
    abstract val offset: TimeSpan
    abstract val iterations: Int

    override fun applyEffect(player: Player, ticks: Int) {
        mineInAbyss.schedule {
            waitFor(offset.inTicks)
            repeat(iterations) {
                applyEffect(player)
                waitFor(duration.inTicks)
            }
            cleanUp(Bukkit.getPlayer(player.uniqueId) ?: player) //get new player reference if player relogged
        }
    }

    abstract fun applyEffect(player: Player)

    override val isDone: Boolean
        get() = duration.inMillis <= 0

    override fun cleanUp(player: Player) {}
}
