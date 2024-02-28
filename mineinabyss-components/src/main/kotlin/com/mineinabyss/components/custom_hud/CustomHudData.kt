package com.mineinabyss.components.custom_hud

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("mineinabyss:custom_hud_data")
data class CustomHudData(
    var showBackgrounds: Boolean = true,
    var alwaysShowAir: Boolean = true,
    var alwaysShowArmor: Boolean = true,
    var showStarCompassHud: Boolean = true,
    var showDepthMeterHud: Boolean = true
)
val Player.customHudData get() = toGeary().getOrSetPersisting { CustomHudData() }
