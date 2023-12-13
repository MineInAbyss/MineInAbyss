package com.mineinabyss.components.players

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player
import java.time.Month

@Serializable
@SerialName("mineinabyss:patreon")
data class Patreon(
    var tier: Int = 1,
    var kitUsedMonth: Month? = null
)
