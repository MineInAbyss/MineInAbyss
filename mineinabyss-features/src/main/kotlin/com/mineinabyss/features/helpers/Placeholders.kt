package com.mineinabyss.features.helpers

import com.mineinabyss.components.custom_hud.customHudData
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.tools.depthmeter.getAbyssDepth
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.math.atan2

class Placeholders : PlaceholderExpansion() {

    override fun getIdentifier() = "mineinabyss"

    override fun getAuthor() = "MineInAbyss"

    override fun getVersion() = "0.10"

    override fun onPlaceholderRequest(player: Player, identifier: String) =
        when (identifier) {
            "orthbanking_coins" -> player.playerDataOrNull?.orthCoinsHeld ?: 0
            "orthbanking_tokens" -> player.playerDataOrNull?.mittyTokensHeld ?: 0

            "hud_orthbanking" -> player.playerDataOrNull?.showPlayerBalance
            "hud_depthmeter" -> player.toGeary().has<ShowDepthMeterHud>()
            "hud_starcompass" -> player.toGeary().has<ShowStarCompassHud>()
            "hud_always_air" -> player.customHudData.alwaysShowAir
            "hud_always_armor" -> player.customHudData.alwaysShowArmor
            "hud_show_top_bar" -> (player.toGeary().has<ShowDepthMeterHud>() && player.toGeary().has<ShowStarCompassHud>())
            "hud_hide_armor_air_background" -> (!player.customHudData.alwaysShowAir && !player.customHudData.alwaysShowArmor)
                    || ((player.getAttribute(Attribute.ARMOR)?.value ?: 0.0) > 0.0) || (player.remainingAir < player.maximumAir)

            "layer" -> player.location.layer?.name
            "layer_simple" -> player.simpleLayerName
            "whistle" -> player.getLayerWhistleForHud()
            "section" -> player.location.section?.name ?: "Unmanaged Section"
            "depth" -> player.location.getAbyssDepth()

            "compass" -> player.getCompassAngle().unicode
            "compass_angle" -> player.getCompassAngle().angle
            else -> null
        }.toString()

    private class CompassAngle(val unicode: String, val angle: String? = null)

    private fun Player.getCompassAngle(): CompassAngle {
        val loc = location.section?.centerLocation ?: return CompassAngle("\uEBBF")
        if (world != loc.world) return CompassAngle("\uEBBF")

        val dir = loc.subtract(location).toVector()
        val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
        val angleLook = (atan2(location.direction.z, location.direction.x) / 2 / Math.PI * 360 + 180) % 360


        return barUnicodeList[(((angleDir - angleLook + 360) % 360) / 22.5).toInt()]
    }

    // Don't alter this list unless you know what you're doing
    private val barUnicodeList = listOf(
        CompassAngle("\uEBB7", "S"),
        CompassAngle("\uEBB6", "SSE"),
        CompassAngle("\uEBB5", "SE"),
        CompassAngle("\uEBB4", "ESE"),
        CompassAngle("\uEBB3", "E"),
        CompassAngle("\uEBB2", "ENE"),
        CompassAngle("\uEBB1", "NE"),
        CompassAngle("\uEBB0", "NNE"),
        CompassAngle("\uEBAF", "N"),
        CompassAngle("\uEBBE", "NNW"),
        CompassAngle("\uEBBD", "NW"),
        CompassAngle("\uEBBC", "WNW"),
        CompassAngle("\uEBBB", "W"),
        CompassAngle("\uEBBA", "WSW"),
        CompassAngle("\uEBB9", "SW"),
        CompassAngle("\uEBB8", "SSW"),
    )
}
