package com.mineinabyss.components.cosmetics

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:cosmetics")
data class CosmeticComponent(
    val gesture: String = "",
    val cosmeticBackpack: String? = null,
)

val Player.cosmeticComponent get() = toGeary().getOrSetPersisting<CosmeticComponent> { CosmeticComponent() }
