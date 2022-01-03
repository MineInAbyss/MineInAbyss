@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.curse.effects

import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import kotlin.time.Duration

//TODO not sure if anything else needs to be updated with this
@Serializable
@SerialName("particles")
data class ParticleAscensionEffect constructor(
    val count: Int,
    override val offset: Duration,
    override val duration: Duration,
    override val iterations: Int,
    private val particles: List<Particle>
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) {
        particles.forEach { addParticlesAroundHead(player, it) }
    }

    private fun addParticlesAroundHead(player: Player, particle: Particle) {
        val (x, y, z) = player.eyeLocation
        val particleData: Any = when (particle) {
            Particle.REDSTONE -> Particle.DustOptions(Color.RED, 1f)
            else -> Unit
        }
        if (particleData == Unit)
            player.spawnParticle(particle, x, y, z, count, .5, .5, .5)
        else
            player.spawnParticle(particle, x, y, z, count, .5, .5, .5, particleData)
    }

    override fun clone() = copy()
}
