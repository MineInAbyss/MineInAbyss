package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.mineinabyss.player.PlayerData
import org.bukkit.entity.Player
import java.util.*

/**
 * Stores context for the plugin, such as the plugin instance
 */
object AbyssContext {
    val playerDataMap = mutableMapOf<UUID, PlayerData>()
    val logger = mineInAbyss.logger

    fun getPlayerData(player: Player): PlayerData {
        return playerDataMap[player.uniqueId] ?: error("Player data not found")
    }
}

val Player.playerData get() = AbyssContext.getPlayerData(this)