package com.mineinabyss.plugin

import com.charleskorn.kaml.AnchorsAndAliases
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.loadAllCatching
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.ansible.ConfigPullFeature
import com.mineinabyss.features.anticheese.AntiCheeseFeature
import com.mineinabyss.features.core.CoreFeature
import com.mineinabyss.features.cosmetics.CosmeticsFeature
import com.mineinabyss.features.curse.CurseFeature
import com.mineinabyss.features.custom_hud.CustomHudFeature
import com.mineinabyss.features.descent.DescentFeature
import com.mineinabyss.features.layers.LayersFeature
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.features.MainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.features.singlePluginLogger
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database


class MineInAbyssPlugin : JavaPlugin(), DI {
    override val di = DI {
        singlePluginLogger(this@MineInAbyssPlugin)
        singleConfig<AbyssFeatureConfig>("config.yml") {
            format = Yaml(
                serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module,
                configuration = YamlConfiguration(anchorsAndAliases = AnchorsAndAliases.Permitted())
            )
        }
        single {
            MainCommand(
                names = listOf("mineinabyss", "mia"),
                description = "The main command for Mine in Abyss"
            )
        }
        // Exposed database connection
        // TODO migrate to our own DB library
        single { Database.connect("jdbc:sqlite:" + this@MineInAbyssPlugin.dataFolder.path + "/data.db", "org.sqlite.JDBC") }
    }

    override fun onLoad() {
        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
        //TODO inject AbyssContext
    }

    override fun onEnable() {
        gearyPaper.configure {
            world.autoscan {
                scan(
                    this@MineInAbyssPlugin.classLoader,
                    listOf(
                        "com.mineinabyss.features",
                        "com.mineinabyss.components",
                        "com.mineinabyss.mineinabyss.core"
                    )
                ) {
                    components()
                    subClassesOf<AscensionEffect>()
                }

            }
        }
        di.scope.loadAllCatching(
            AntiCheeseFeature,
            ConfigPullFeature,
            CoreFeature,
            CosmeticsFeature,
            CurseFeature,
            CustomHudFeature,
            DescentFeature,

            LayersFeature,
            //TODO add remaining features
        )
    }

    override fun onDisable() {
        di.scope.close()
    }
}
