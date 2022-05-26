package com.mineinabyss.components.cosmetics

import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:cosmetics")
data class Cosmetics(
    val gesture: String? = null,
    val cosmeticBackpack: String? = null
)

val Player.cosmetics get() = toGeary().getOrSetPersisting { Cosmetics() }
