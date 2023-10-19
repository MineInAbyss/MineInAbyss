package com.mineinabyss.plugin

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyssFeatures
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.Guilds
import com.mineinabyss.features.guilds.database.Players
import com.mineinabyss.features.helpers.Placeholders
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.idofront.config.ConfigFormats
import com.mineinabyss.idofront.config.Format
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.messaging.logError
import com.mineinabyss.idofront.messaging.logSuccess
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.actions
import com.mineinabyss.mineinabyss.core.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

val featureManager by DI.observe<FeatureFeature>()

// very meta
class FeatureFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() = actions {
        DI.add<AbyssContext>(object : AbyssContext(this@enableFeature) {})

        val featureConfigController = config<AbyssFeatureConfig>(
            "config", abyss.dataPath, AbyssFeatureConfig(), formats = ConfigFormats(
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
        DI.addByDelegate<AbyssFeatureConfig> { featureConfigController.getOrLoad() }

        "Registering feature contexts" {
            abyssFeatures.features
                .filterIsInstance<AbyssFeatureWithContext<*>>()
                .forEach {
                    runCatching { it.createAndInjectContext() }
                        .onFailure { e -> logError("Failed to create context for ${it::class.simpleName}: $e") }
                }
        }
        abyssFeatures.features.forEach { feature ->
            feature.apply {
                val featureName = feature::class.simpleName
                when (dependsOn.all { Plugins.isEnabled(it) }) {
                    true -> "Enabled $featureName" {
                        abyss.plugin.loadFeature()
                        abyss.plugin.enableFeature()
                    }.onFailure(Throwable::printStackTrace)

                    false -> logError(
                        "Could not enable $featureName, missing dependencies: ${dependsOn.filterNot(Plugins::isEnabled)}"
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

        commands {
            mineinabyss {
                "reload" {
                    disable(this@enableFeature)
                    enable(this@enableFeature)
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("reload").filter { it.startsWith(args[0], ignoreCase = true) }
                    else -> null
                }
            }
        }
    }

    override fun MineInAbyssPlugin.disableFeature() = actions {
        "Disabling features" {
            abyssFeatures.features.forEach { feature ->
                feature.apply {
                    runCatching { abyss.plugin.disableFeature() }
                        .onSuccess { logSuccess("Disabled ${feature::class.simpleName}") }
                        .onFailure { e -> logError("Failed to disable ${feature::class.simpleName}: $e") }
                }
            }
            DI.remove<AbyssContext>()
            DI.remove<AbyssFeatureConfig>()
        }
    }
}
