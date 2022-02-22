package com.mineinabyss.components.relics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Particle
import org.bukkit.Sound

@Serializable
@SerialName("mineinabyss:bounding_lance")
class BoundingLance(
    val placeSound: Sound = Sound.BLOCK_CHAIN_PLACE,
    val effectRadius: Double = 1.5,
    val effectDuration: Long = 10,
    val effectParticles: List<Particle> = listOf(Particle.DRIP_LAVA)
)