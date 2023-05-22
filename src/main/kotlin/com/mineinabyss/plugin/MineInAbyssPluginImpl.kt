package com.mineinabyss.plugin

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.guilds.database.GuildJoinQueue
import com.mineinabyss.guilds.database.GuildMessageQueue
import com.mineinabyss.guilds.database.Guilds
import com.mineinabyss.guilds.database.Players
import com.mineinabyss.helpers.Placeholders
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
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

    override fun onEnable() {
        saveDefaultConfig()


        geary {
            // TODO change package to features!
            autoscan(classLoader, "com.mineinabyss") {
                components()
                subClassesOf<AbyssFeature>()
                subClassesOf<AscensionEffect>()
            }
            on(GearyPhase.ENABLE) {
                createAbyssContext()
            }
        }

        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")

        transaction(abyss.db) {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, GuildMessageQueue)
        }

        if (abyss.isPlaceholderApiLoaded) {
            Placeholders().register()
        }

        actions {
            "Enabling features" {
                abyss.config.features.forEach {
                    it.apply {
                        val featureName = it::class.simpleName
                        attempt(success = "Enabled $featureName", fail = "Failed to enable $featureName") {
                            abyss.plugin.enableFeature()
                        }.onFailure(Throwable::printStackTrace)
                    }
                }
            }
        }
    }

    override fun onDisable() = actions {
        "Disabling features" {
            abyss.config.features.forEach {
                it.apply {
                    "Disabled ${it::class.simpleName}" {
                        abyss.plugin.disableFeature()
                    }
                }
            }
        }
    }

    fun createAbyssContext() {
        DI.remove<AbyssContext>()
        val config: AbyssConfig by config<AbyssConfig>("config") {
            formats {
                mapOf(
                    "yml" to Yaml(
                        // We autoscan in our Feature classes so need to use Geary's module.
                        serializersModule = serializableComponents.serializers.module,
                        configuration = YamlConfiguration(extensionDefinitionPrefix = "x-")
                    )
                )
            }
            fromPluginPath(loadDefault = true)
        }

        DI.add<AbyssContext>(AbyssContext(this, config, AbyssWorldManagerImpl()))

    }
}
