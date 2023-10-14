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
import com.mineinabyss.idofront.messaging.logSuccess
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
                subClassesOf<AscensionEffect>()
            }

            on(GearyPhase.ENABLE) {
                DI.add(FeatureFeature())
                featureManager.enable(this@MineInAbyssPluginImpl)
            }
        }

        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
    }

    override fun onDisable() {
        featureManager.disable(this@MineInAbyssPluginImpl)
    }
}
