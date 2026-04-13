package com.mineinabyss.features

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.plugin.Plugin
import org.jetbrains.exposed.v1.jdbc.Database

val abyss get() = AbyssContext.instance ?: error("MineInAbyss plugin hasn't started yet!")

interface AbyssContext : Plugin, com.mineinabyss.dependencies.DI {
    val logger: ComponentLogger

    @Deprecated("Use gearyPaper", ReplaceWith("gearyPaper.worldManager.global"))
    val gearyGlobal get() = gearyPaper.worldManager.global
    val config: AbyssFeatureConfig
    val isChattyLoaded get() = server.pluginManager.isPluginEnabled("chatty")
    val isEternalFortuneLoaded get() = server.pluginManager.isPluginEnabled("EternalFortune")
    val isPlaceholderApiLoaded get() = server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isHMCCosmeticsEnabled get() = server.pluginManager.isPluginEnabled("HMCCosmetics")
    val isModelEngineEnabled get() = server.pluginManager.isPluginEnabled("ModelEngine")
    val db: Database

    companion object {
        var instance: AbyssContext? = null
    }
}
