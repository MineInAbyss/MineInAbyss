package com.derongan.minecraft.mineinabyss.ascension.effect.effects

import com.derongan.minecraft.mineinabyss.ascension.effect.AbstractAscensionEffect
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Particle
import org.bukkit.entity.Player

//TODO not sure if anything else needs to be updated with this
@Serializable
@SerialName("particles")
data class ParticleAscensionEffect(
        val strength: Int,
        override val offset: Long,
        override val duration: Int,
        override val iterations: Int,
        private val particles: List<Particle>
) : AbstractAscensionEffect() {
    override fun applyEffect(player: Player) {
        particles.forEach { addParticlesAroundHead(player, it) }
    }

    private fun addParticlesAroundHead(player: Player, particle: Particle) {
        val (x, y, z) = player.eyeLocation
        player.spawnParticle(particle, x, y, z, strength * 5, .5, .5, .5, 0f)
    }
    override fun clone() = copy()
}