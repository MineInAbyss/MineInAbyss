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
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.observeLogger
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import java.nio.file.Path

val abyss by DI.observe<AbyssContext>()

class AbyssContext(
    override val plugin: JavaPlugin,
) : FeatureDSL(mainCommandProvider = { ("mineinabyss" / "mia")(desc = "The main command for Mine in Abyss", it) }),
    Configurable<AbyssFeatureConfig> {
    val logger by plugin.observeLogger()
    val dataPath: Path = plugin.dataFolder.toPath()
    val gearyGlobal get() = gearyPaper.worldManager.global

    override val configManager = config<AbyssFeatureConfig>(
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

    override val features get() = config.features


    val isChattyLoaded get() = plugin.server.pluginManager.isPluginEnabled("chatty")
    val isEternalFortuneLoaded get() = plugin.server.pluginManager.isPluginEnabled("EternalFortune")
    val isPlaceholderApiLoaded get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isHMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("HMCCosmetics")
    val isModelEngineEnabled get() = plugin.server.pluginManager.isPluginEnabled("ModelEngine")

    val db = Database.connect("jdbc:sqlite:" + plugin.dataFolder.path + "/data.db", "org.sqlite.JDBC")
}
