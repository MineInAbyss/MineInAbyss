package com.mineinabyss.features

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.featureManager
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

    val configManager = config<AbyssFeatureConfig>(
        "config", dataPath, AbyssFeatureConfig(), formats = ConfigFormats(
            overrides = listOf(
                Format(
                    "yml", Yaml(
                        // We autoscan in our Feature classes so need to use Geary's module.
                        serializersModule = gearyGlobal.getAddon(SerializableComponents).serializers.module,
                        configuration = YamlConfiguration(anchorsAndAliases = AnchorsAndAliases.Permitted())
                    )
                )
            )
        )
    )
    val config: AbyssFeatureConfig by configManager

    val featureManager = featureManager {
        globalModule {
            single<AbyssContext> { abyss }
        }

        withMainCommand("mineinabyss", "mia", description = "The main command for Mine in Abyss")

        install(*abyss.config.features.toTypedArray())
    }

    inline fun <reified T: Any> getScoped(feature: Feature): T {
        return featureManager.getScope(feature).get<T>()
    }

    val isChattyLoaded get() = plugin.server.pluginManager.isPluginEnabled("chatty")
    val isEternalFortuneLoaded get() = plugin.server.pluginManager.isPluginEnabled("EternalFortune")
    val isPlaceholderApiLoaded get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isHMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("HMCCosmetics")
    val isModelEngineEnabled get() = plugin.server.pluginManager.isPluginEnabled("ModelEngine")

    val db = Database.connect("jdbc:sqlite:" + plugin.dataFolder.path + "/data.db", "org.sqlite.JDBC")
}
