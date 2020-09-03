package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.mineinabyss.configuration.MineInAbyssConfig
import com.derongan.minecraft.mineinabyss.player.PlayerData
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl
import com.derongan.minecraft.mineinabyss.world.Layer
import org.bukkit.Location
import org.bukkit.configuration.Configuration
import org.bukkit.entity.Player
import java.util.*
import java.util.logging.Logger

/**
 * Stores context for the plugin, such as the plugin instance
 */
object AbyssContext {
    val playerDataMap: Map<UUID, PlayerData> = HashMap()
    val logger: Logger = mineInAbyss.logger
    val config: Configuration = mineInAbyss.config
    val worldManager: AbyssWorldManager = AbyssWorldManagerImpl(config)
    val configManager: MineInAbyssConfig = MineInAbyssConfig(mineInAbyss, this)

    fun getPlayerData(player: Player): PlayerData {
        return playerDataMap[player.uniqueId] ?: error("Player data not found")
    }

    fun getLayerForLocation(loc: Location): Layer? {
        return worldManager.getLayerForSection(WorldManager.getSectionFor(loc) ?: return null)
    }
}

val Player.playerData get() = AbyssContext.getPlayerData(this)