package com.mineinabyss.features.helpers

import com.mineinabyss.components.huds.AlwaysShowAirHud
import com.mineinabyss.components.huds.ReturnVanillaHud
import com.mineinabyss.components.playerData
import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.tools.depthmeter.getDepth
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.mineinabyss.core.layer
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import kotlin.math.atan2
import kotlin.math.roundToInt

class Placeholders : PlaceholderExpansion() {

    override fun getIdentifier() = "mineinabyss"

    override fun getAuthor() = "MineInAbyss"

    override fun getVersion() = "0.10"

    override fun onPlaceholderRequest(player: Player, identifier: String): String {
        player.mineinabyssPlaceholders.forEach {
            if (identifier == it.key) {
                return it.value
            }
        }
        return identifier
    }

    private val Player.mineinabyssPlaceholders: Map<String, String>
        get() = mapOf(
            "orthbanking_coins" to playerData.orthCoinsHeld.toString(),
            "orthbanking_tokens" to playerData.mittyTokensHeld.toString(),

            "hud_orthbanking" to playerData.showPlayerBalance.toString(),
            "hud_depthmeter" to toGeary().has<ShowDepthMeterHud>().toString(),
            "hud_starcompass" to toGeary().has<ShowStarCompassHud>().toString(),
            "hud_always_air" to toGeary().has<AlwaysShowAirHud>().toString(),
            "hud_always_armor" to toGeary().has<AlwaysShowAirHud>().toString(),
            "hud_vanilla" to toGeary().has<ReturnVanillaHud>().toString(),

            "layer" to (location.layer?.name ?: "").toString(),
            "layer_simple" to simpleLayerName,
            "whistle" to getLayerWhistleForHud(),
            "section" to (location.section?.name ?: "Unmanaged Section").toString(),
            "depth" to getDepth().toString(),
            "starcompass_unicode" to getCompassAngle().first,
            "starcompass_angle" to getCompassAngle().second.toString(),

            "temperature" to location.block.temperature.times(10).roundToInt().toString(),
            "humidity" to location.block.humidity.times(10).roundToInt().toString(),
            "time" to world.time.toString(),
            "fulltime" to world.fullTime.toString(),

            //"mount_health" to mount?.health.toString(),
            //"mount_health_rounded" to mount?.health?.roundToInt().toString(),
            //"mount_health_max" to mount?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value.toString()
        )

    private fun Player.getCompassAngle(): Pair<String, String?> {
        val loc = location.section?.centerLocation ?: return "\uEBBF" to null
        if (world != loc.world) return "\uEBBF" to null

        val dir = loc.subtract(location).toVector()
        val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
        val angleLook = (atan2(location.direction.z, location.direction.x) / 2 / Math.PI * 360 + 180) % 360

        return barUnicodeList[(((angleDir - angleLook + 360) % 360) / 22.5).toInt()]
    }

    // Don't alter this list unless you know what you're doing
    private val barUnicodeList = listOf(
        "\uEBB7" to "S",
        "\uEBB6" to "SSE",
        "\uEBB5" to "SE",
        "\uEBB4" to "ESE",
        "\uEBB3" to "E",
        "\uEBB2" to "ENE",
        "\uEBB1" to "NE",
        "\uEBB0" to "NNE",
        "\uEBAF" to "N",
        "\uEBBE" to "NNW",
        "\uEBBD" to "NW",
        "\uEBBC" to "WNW",
        "\uEBBB" to "W",
        "\uEBBA" to "WSW",
        "\uEBB9" to "SW",
        "\uEBB8" to "SSW",
    )
}
