package com.mineinabyss.features

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.Services
import github.scarsz.discordsrv.DiscordSRV
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import java.nio.file.Path

/** A reference to the MineInAbyss plugin */
//val mineInAbyss: MineInAbyssPlugin by lazy { Bukkit.getPluginManager().getPlugin("MineInAbyss") as MineInAbyssPlugin }
val discordSRV: DiscordSRV by lazy { Bukkit.getPluginManager().getPlugin("DiscordSRV") as DiscordSRV }
val abyss by DI.observe<AbyssContext>()

class AbyssContext(
    override val plugin: JavaPlugin,
) : FeatureDSL(mainCommandProvider = { ("mineinabyss" / "mia")(desc = "The main command for Mine in Abyss", it) }),
    Configurable<AbyssFeatureConfig> {
    val dataPath: Path = plugin.dataFolder.toPath()

    override val configManager = config<AbyssFeatureConfig>(
        "config", dataPath, AbyssFeatureConfig(), formats = ConfigFormats(
            overrides = listOf(
                Format(
                    "yml", Yaml(
                        // We autoscan in our Feature classes so need to use Geary's module.
                        serializersModule = serializableComponents.serializers.module,
                        configuration = YamlConfiguration(allowAnchorsAndAliases = true)
                    )
                )
            )
        )
    )

    override val features get() = config.features


    val isChattyLoaded get() = plugin.server.pluginManager.isPluginEnabled("chatty")
    val isGSitLoaded get() = plugin.server.pluginManager.isPluginEnabled("GSit")
    val isPlaceholderApiLoaded get() = plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")
    val isHMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("HMCCosmetics")
    val isMCCosmeticsEnabled get() = plugin.server.pluginManager.isPluginEnabled("MCCosmetics")
    val isModelEngineEnabled get() = plugin.server.pluginManager.isPluginEnabled("ModelEngine")
    val isMobzyEnabled get() = plugin.server.pluginManager.isPluginEnabled("Mobzy")

    val econ: Economy? = Services.getOrNull<Economy>()

    val db = Database.connect("jdbc:sqlite:" + plugin.dataFolder.path + "/data.db", "org.sqlite.JDBC")
}
