package com.mineinabyss.features.helpers

import com.mineinabyss.components.custom_hud.customHudData
import com.mineinabyss.components.playerData
import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.tools.depthmeter.getDepth
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ArmorMeta
import kotlin.math.atan2
import kotlin.math.roundToInt

class Placeholders : PlaceholderExpansion() {

    override fun getIdentifier() = "mineinabyss"

    override fun getAuthor() = "MineInAbyss"

    override fun getVersion() = "0.10"

    override fun onPlaceholderRequest(player: Player, identifier: String) =
        player.mineinabyssPlaceholders.firstOrNull { it.identifier == identifier }?.value?.toString() ?: identifier

    class Placeholder(val identifier: String, val value: Any)

    private val Player.mineinabyssPlaceholders: Set<Placeholder> get() = setOf(
        Placeholder("orthbanking_coins", playerData.orthCoinsHeld),
        Placeholder("orthbanking_tokens", playerData.mittyTokensHeld),

        Placeholder("hud_orthbanking", playerData.showPlayerBalance),
        Placeholder("hud_depthmeter", toGeary().has<ShowDepthMeterHud>()),
        Placeholder("hud_starcompass", toGeary().has<ShowStarCompassHud>()),
        Placeholder("hud_always_air", customHudData.alwaysShowAir),
        Placeholder("hud_always_armor", customHudData.alwaysShowArmor),
        Placeholder("hud_show_top_bar", toGeary().apply { has<ShowDepthMeterHud>() && has<ShowStarCompassHud>() }),
        Placeholder("hud_hide_armor_air_background", ((!customHudData.alwaysShowAir && !customHudData.alwaysShowArmor)
                || ((getAttribute(Attribute.GENERIC_ARMOR)?.value ?: 0.0) > 0.0) || (remainingAir < maximumAir))
        ),

        Placeholder("layer", (location.layer?.name ?: "")),
        Placeholder("layer_simple", simpleLayerName),
        Placeholder("whistle", getLayerWhistleForHud()),
        Placeholder("section", (location.section?.name ?: "Unmanaged Section")),
        Placeholder("depth", getDepth()),
        Placeholder("starcompass_unicode", getCompassAngle().unicode),
        Placeholder("starcompass_angle", getCompassAngle().angle ?: "null"),

        Placeholder("temperature", location.block.temperature.times(10).roundToInt()),
        Placeholder("humidity", location.block.humidity.times(10).roundToInt()),
        Placeholder("time", world.time),
        Placeholder("fulltime", world.fullTime),
    )

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
