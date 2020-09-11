package com.derongan.minecraft.mineinabyss.configuration

import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.player.PlayerDataConfigManager
import java.io.File

class MineInAbyssConfig(private val plugin: MineInAbyss) {
    val startLocationCM: ConfigManager
    val playerDataCM: PlayerDataConfigManager

    private fun createConfig() {
        try {
            if (!plugin.dataFolder.exists()) {
                if (!plugin.dataFolder.mkdirs()) {
                    throw RuntimeException("Failed to make config file")
                }
            }
            val file = File(plugin.dataFolder, "config.yml")
            if (!file.exists()) {
                plugin.logger.info("Config.yml not found, creating!")
                plugin.saveDefaultConfig()
            } else {
                plugin.logger.info("Config.yml found, loading!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveAll() {
        startLocationCM.saveConfig()
        playerDataCM.saveConfig()
    }

    fun reloadAll() {
        createConfig()
        startLocationCM.reload()
    }

    init {
        createConfig()
        startLocationCM = ConfigManager(plugin, File(plugin.dataFolder, "spawn-locs.yml"))
        playerDataCM = PlayerDataConfigManager(AbyssContext)
    }
}