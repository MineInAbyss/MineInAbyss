package com.mineinabyss.features.curse.effects

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.entities.toPlayer
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import kotlin.time.Duration

@Serializable
sealed class AbstractAscensionEffect : AscensionEffect {
    abstract val offset: Duration
    abstract val iterations: Int

    override fun applyEffect(player: Player, ticks: Int) {
        abyss.plugin.launch {
            delay(offset)
            repeat(iterations) {
                applyEffect(player)
                delay(duration)
            }
            cleanUp(player.uniqueId.toPlayer() ?: player) //get new player reference if player relogged
        }
    }

    abstract fun applyEffect(player: Player)

    override val isDone: Boolean
        get() = duration.inWholeMilliseconds <= 0

    override fun cleanUp(player: Player) {}
}
