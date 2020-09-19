package com.derongan.minecraft.mineinabyss.configuration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.player.PlayerData
import com.derongan.minecraft.mineinabyss.player.PlayerDataImpl
import com.google.common.annotations.VisibleForTesting
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.IOException
import java.nio.file.Path

//TODO idofront config system for saving data associated with UUIDs
object PlayerDataConfig {
    fun loadPlayerData(player: Player): PlayerData {
        val path = getPlayerDataPath(player).toFile()
        return if (path.exists())
            Yaml(configuration = YamlConfiguration(
                    strictMode = false //ignore unnecessary old tags in player data
            )).decodeFromString(PlayerDataImpl.serializer(), path.readLines().joinToString(separator = "\n"))
        else PlayerDataImpl(player.uniqueId)
    }

    fun savePlayerData(playerData: PlayerData) {
        if (playerData !is PlayerDataImpl) TODO("Add support for other implementations of PlayerData")
        val path = getPlayerDataPath(playerData.player).toFile()
        path.parentFile.mkdirs()

        path.writeText(Yaml(configuration = YamlConfiguration(
                encodeDefaults = false
        )).encodeToString(PlayerDataImpl.serializer(), playerData))
    }

    @VisibleForTesting
    fun getPlayerDataPath(player: Player): Path {
        val uuid = player.uniqueId
        return mineInAbyss.dataFolder
                .toPath()
                .resolve(ConfigConstants.PLAYER_DATA_DIR)
                .resolve("$uuid.yml")
    }

    fun saveAll() {
        Bukkit.getServer().onlinePlayers.forEach { player: Player ->
            val data = AbyssContext.getPlayerData(player)
            try {
                savePlayerData(data)
            } catch (e: IOException) {
                AbyssContext.logger.warning("Error saving player data for " + player.uniqueId)
                e.printStackTrace()
            }
        }
    }

    init {
        Bukkit.getServer().onlinePlayers.forEach { player -> AbyssContext.playerDataMap[player.uniqueId] = loadPlayerData(player) }
    }
}