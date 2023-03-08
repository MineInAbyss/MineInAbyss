package com.mineinabyss.components.cosmetics

import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:cosmetics")
data class CosmeticComponent(
    val gesture: String = "",
    val cosmeticBackpack: String = ""
)

val Player.cosmeticComponent get() = toGeary().getOrSetPersisting { CosmeticComponent() }
