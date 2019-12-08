package com.derongan.minecraft.mineinabyss

import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.mineinabyss.configuration.MineInAbyssConfig
import com.derongan.minecraft.mineinabyss.player.PlayerData
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManagerImpl
import com.derongan.minecraft.mineinabyss.world.Layer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.Configuration
import org.bukkit.entity.Player
import java.util.*
import java.util.logging.Logger

/**
 * Stores context for the plugin, such as the plugin instance
 */
class AbyssContext(val plugin: MineInAbyss) {
    val playerDataMap: Map<UUID, PlayerData> = HashMap()
    val logger: Logger = plugin.logger
    val config: Configuration = plugin.config
    val realWorldManager: WorldManager = Bukkit.getServicesManager().load(WorldManager::class.java)
            ?: error("World manager not found")
    val worldManager: AbyssWorldManager = AbyssWorldManagerImpl(config)
    val configManager: MineInAbyssConfig = MineInAbyssConfig(plugin, this)


    fun getPlayerData(player: Player): PlayerData {
        return playerDataMap[player.uniqueId] ?: error("Player data not found")
    }

    fun getLayerForLocation(loc: Location?): Layer {
        return worldManager.getLayerForSection(realWorldManager.getSectionFor(loc))
    }
}

private val instance by lazy { MineInAbyss.getContext() }

fun getPlayerData(player: Player): PlayerData {
    return instance.getPlayerData(player)
}

fun getLayerForLocation(loc: Location?): Layer {
    return instance.getLayerForLocation(loc)
}