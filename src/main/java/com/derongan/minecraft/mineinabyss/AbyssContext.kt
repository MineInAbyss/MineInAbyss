package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.mineinabyss.player.PlayerData
import com.mineinabyss.idofront.plugin.getServiceOrNull
import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player
import java.util.*

/**
 * Stores context for the plugin, such as the plugin instance
 */
internal object AbyssContext {
    val playerDataMap = mutableMapOf<UUID, PlayerData>()
    val logger = mineInAbyss.logger
    val econ by lazy { getServiceOrNull<Economy>("Vault") }

    fun getPlayerData(player: Player): PlayerData {
        return playerDataMap[player.uniqueId] ?: error("Player data not found")
    }
}

val Player.playerData get() = AbyssContext.getPlayerData(this)
