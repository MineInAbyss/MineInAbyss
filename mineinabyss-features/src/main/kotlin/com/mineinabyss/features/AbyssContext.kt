package com.mineinabyss.features

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.observeLogger
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import java.nio.file.Path

val abyss by DI.observe<AbyssContext>()

class AbyssContext(
    val plugin: JavaPlugin,
) {
    val logger by plugin.observeLogger()
    val dataPath: Path = plugin.dataFolder.toPath()
    val gearyGlobal get() = gearyPaper.worldManager.global
    val config: AbyssFeatureConfig get() = TODO()
    val isChattyLoaded get() = plugin.server.pluginManager.isPluginEnabled("chatty")
    val isEternalFortuneLoaded get() = plugin.server.pluginManager.isPluginEnabled("EternalFortune")
    val isPlaceholderApiLoaded get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isHMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("HMCCosmetics")
    val isModelEngineEnabled get() = plugin.server.pluginManager.isPluginEnabled("ModelEngine")

    val db = Database.connect("jdbc:sqlite:" + plugin.dataFolder.path + "/data.db", "org.sqlite.JDBC")
}
