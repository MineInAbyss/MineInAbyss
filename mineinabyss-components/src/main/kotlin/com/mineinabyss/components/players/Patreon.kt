package com.mineinabyss.components.players

import com.mineinabyss.geary.papermc.access.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import java.time.Month

@Serializable
@SerialName("mineinabyss:patreon")
data class Patreon(
    val tier: Int = 1,
    val kitUsedMonth: Month? = null
)

val Player.patreon get() = toGeary().get<Patreon>()
