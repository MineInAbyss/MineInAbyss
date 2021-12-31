@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.curse.effects

import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.entity.Player
import kotlin.time.Duration

@Serializable
@SerialName("death")
data class DeathAscensionEffect(
    override val offset: Duration = 0.ticks,
    override val duration: Duration = 0.ticks,
    override val iterations: Int = 0
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) {
        player.health = 0.0
    }

    override fun clone() = copy()
}
