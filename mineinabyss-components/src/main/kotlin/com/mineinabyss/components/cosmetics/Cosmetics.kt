package com.mineinabyss.components.cosmetics

import com.mineinabyss.geary.papermc.access.toGeary
import io.lumine.cosmetics.managers.gestures.Gesture
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:cosmetics")
class Cosmetics(
    var gesture: @Contextual Gesture? = null
)

val Player.cosmetics get() = toGeary().getOrSetPersisting { Cosmetics() }
