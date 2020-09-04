package com.derongan.minecraft.mineinabyss.ascension.effect.effects

import com.derongan.minecraft.mineinabyss.ascension.effect.AbstractAscensionEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("death")
data class DeathAscensionEffect(
        override val offset: Long = 0,
        override val duration: Int = 0,
        override val iterations: Int = 0
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) {
        player.health = 0.0
    }

    override fun clone() = copy()
}