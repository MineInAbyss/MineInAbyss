package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.relics.depthmeter.getDepth
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import kotlin.math.roundToInt

class Placeholders : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "mineinabyss"
    }

    override fun getAuthor(): String {
        return "MineInAbyss"
    }

    override fun getVersion(): String {
        return "0.10"
    }

    override fun onPlaceholderRequest(player: Player, identifier: String): String {
        player.mineinabyssPlaceholders().forEach {
            if (identifier == it.key) {
                return it.value
            }
        }
        return identifier
    }
}

fun Player.mineinabyssPlaceholders() : Map<String, String> {
    return mapOf(
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
        //"point_of_interest" to location.getPointOfInterest().toString(),
    )
}
