package com.mineinabyss.curse.effects

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.idofront.time.inWholeTicks
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.time.Duration

@Serializable
sealed class AbstractAscensionEffect : AscensionEffect {
    abstract val offset: Duration
    abstract val iterations: Int

    override fun applyEffect(player: Player, ticks: Int) {
        mineInAbyss.schedule {
            waitFor(offset.inWholeTicks)
            repeat(iterations) {
                applyEffect(player)
                waitFor(duration.inWholeTicks)
            }
            cleanUp(Bukkit.getPlayer(player.uniqueId) ?: player) //get new player reference if player relogged
        }
    }

    abstract fun applyEffect(player: Player)

    override val isDone: Boolean
        get() = duration.inWholeMilliseconds <= 0

    override fun cleanUp(player: Player) {}
}
