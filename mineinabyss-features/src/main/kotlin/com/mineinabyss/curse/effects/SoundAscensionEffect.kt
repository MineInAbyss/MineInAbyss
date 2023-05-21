@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.curse.effects

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import com.mineinabyss.idofront.operators.times
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random
import kotlin.time.Duration

@Serializable
@SerialName("sound")
data class SoundAscensionEffect(
    val sounds: List<String>,
    override val offset: Duration,
    override val iterations: Int,
    override val duration: Duration
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) { //TODO do not play sounds too quickly together
        val soundLocation = player.location + (Vector.getRandom() * 5 - Vector(2.5, 2.5, 2.5))
        val sound = sounds.random()
        abyss.plugin.launch {
            delay(Random.nextLong(10).ticks)
            player.playSound(soundLocation, sound, 1f, 1f)
        }
    }

    override fun clone() = copy()
}
