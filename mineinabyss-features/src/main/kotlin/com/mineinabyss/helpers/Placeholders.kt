package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.helpers.MountUtils.mount
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.relics.depthmeter.getDepth
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.attribute.Attribute
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

            "layer" to (location.layer?.name ?: "").toString(),
            "whistle" to getLayerWhistleForHud(),
            "section" to (location.section?.name ?: "Unmanaged Section").toString(),
            "depth" to getDepth().toString(),
            "starcompass" to getCompassAngleUnicode(),

            "temperature" to location.block.temperature.times(10).roundToInt().toString(),
            "humidity" to location.block.humidity.times(10).roundToInt().toString(),
            "time" to world.time.toString(),
            "fulltime" to world.fullTime.toString(),

            "mount_health" to mount?.health.toString(),
            "mount_health_rounded" to mount?.health?.roundToInt().toString(),
            "mount_health_max" to mount?.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value.toString()
        )

    private fun Player.getCompassAngleUnicode(): String {
        val loc = location.section?.centerLocation ?: return "\uEBBF"
        if (world != loc.world) return "\uEBBF"

        val dir = loc.subtract(location).toVector()
        val angleDir = (atan2(dir.z, dir.x) / 2 / Math.PI * 360 + 180) % 360
        val angleLook = (atan2(location.direction.z, location.direction.x) / 2 / Math.PI * 360 + 180) % 360

        return barUnicodeList[(((angleDir - angleLook + 360) % 360) / 22.5).toInt()]
    }

    // Don't alter this list unless you know what you're doing
    private val barUnicodeList = listOf(
        "\uEBB4",
        "\uEBB3",
        "\uEBB2",
        "\uEBB1",
        "\uEBB0",
        "\uEBAF",
        "\uEBBE",
        "\uEBBD",
        "\uEBBC",
        "\uEBB7",
        "\uEBB6",
        "\uEBB5",
        "\uEBBB",
        "\uEBBA",
        "\uEBB9",
        "\uEBB8",
    )
}
