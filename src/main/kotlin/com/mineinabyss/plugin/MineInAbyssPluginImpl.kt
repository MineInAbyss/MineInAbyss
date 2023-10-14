package com.mineinabyss.plugin

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.Guilds
import com.mineinabyss.features.guilds.database.Players
import com.mineinabyss.features.helpers.Placeholders
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.actions
import com.mineinabyss.mineinabyss.core.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class MineInAbyssPluginImpl : MineInAbyssPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() = actions {
        geary {
            autoscan(
                classLoader,
                "com.mineinabyss.features",
                "com.mineinabyss.components",
                "com.mineinabyss.mineinabyss.core"
            ) {
                components()
                subClassesOf<AbyssFeature>()
                subClassesOf<AscensionEffect>()
            }

            on(GearyPhase.ENABLE) {
                DI.add<AbyssContext>(object : AbyssContext(this@MineInAbyssPluginImpl) {})

                val featureConfigController = config<AbyssFeatureConfig>(
                    "config", abyss.dataPath, AbyssFeatureConfig(), formats = ConfigFormats(
                        overrides = listOf(
                            Format(
                                "yml", Yaml(
                                    // We autoscan in our Feature classes so need to use Geary's module.
                                    serializersModule = serializableComponents.serializers.module,
                                    configuration = YamlConfiguration(
                                        extensionDefinitionPrefix = "x-",
                                        allowAnchorsAndAliases = true,
                                    )
                                )
                            )
                        )
                    )
                )
                DI.addByDelegate<AbyssFeatureConfig> { featureConfigController.getOrLoad() }

                "Registering features for DI" {
                    abyssFeatures.features.forEach { DI.addByClass(it) }
                }
                abyssFeatures.features.forEach { feature ->
                    feature.apply {
                        val featureName = feature::class.simpleName
                        when (dependsOn.all { Plugins.isEnabled(it) }) {
                            true -> "Enabled $featureName" {
                                abyss.plugin.enableFeature()
                            }.onFailure(Throwable::printStackTrace)

                            false -> logError(
                                "Disabled $featureName, missing dependencies: ${
                                    dependsOn.filterNot {
                                        Plugins.isEnabled(
                                            it
                                        )
                                    }
                                }"
                            )
                        }
                    }
                }

                transaction(abyss.db) {
                    addLogger(StdOutSqlLogger)

                    SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, GuildMessageQueue)
                }

                if (abyss.isPlaceholderApiLoaded) {
                    Placeholders().register()
                }
            }
        }

        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
    }

    override fun onDisable() = actions {
        "Disabling features" {
            abyssFeatures.features.forEach {
                it.apply {
                    "Disabled ${it::class.simpleName}" {
                        abyss.plugin.disableFeature()
                    }
                }
            }
        }
    }
}
