package com.derongan.minecraft.mineinabyss.ascension.effect.effects

import com.derongan.minecraft.mineinabyss.ascension.effect.AbstractAscensionEffect
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.operators.plus
import com.mineinabyss.idofront.operators.times
import com.mineinabyss.idofront.time.TimeSpan
import com.okkero.skedule.schedule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.random.Random

@Serializable
@SerialName("sound")
data class SoundAscensionEffect(
    val sounds: List<String>,
    override val offset: TimeSpan,
    override val iterations: Int,
    override val duration: TimeSpan
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) { //TODO do not play sounds too quickly together
        val soundLocation = player.location + (Vector.getRandom() * 5 - Vector(2.5, 2.5, 2.5))
        val sound = sounds.random()
        mineInAbyss.schedule {
            waitFor(Random.nextLong(10))
            player.playSound(soundLocation, sound, 1f, 1f)
        }
    }

    override fun clone() = copy()
}
