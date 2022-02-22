package com.mineinabyss.components.tools

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Particle
import org.bukkit.block.Block

@Serializable
@SerialName("mineinabyss:flamethrower")
class Flamethrower(
    val particle: Particle = Particle.SOUL_FIRE_FLAME,
    val flameReach: Int = 6,
    var fuel: Int = 100,
    var refuelItem: SerializableItemStack? = null,
    val burnableBlocks: List<Block> = listOf()
)