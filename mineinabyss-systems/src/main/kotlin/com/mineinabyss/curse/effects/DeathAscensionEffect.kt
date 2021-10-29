package com.mineinabyss.curse.effects

import com.mineinabyss.idofront.time.TimeSpan
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("death")
data class DeathAscensionEffect(
    override val offset: TimeSpan = 0.ticks,
    override val duration: TimeSpan = 0.ticks,
    override val iterations: Int = 0
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) {
        player.health = 0.0
    }

    override fun clone() = copy()
}
