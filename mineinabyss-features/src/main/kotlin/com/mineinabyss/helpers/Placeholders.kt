package com.mineinabyss.helpers

import com.mineinabyss.components.playerData
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

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
        mineinabyssPlaceholders(player).forEach {
            if (identifier == it.key) {
                return it.value
            }
        }
        return identifier
    }
}

fun mineinabyssPlaceholders(player: Player) : Map<String, String> {
    return mapOf(
        "orthbanking_coins" to player.playerData.orthCoinsHeld.toString(),
        "orthbanking_tokens" to player.playerData.mittyTokensHeld.toString(),
    )
}
