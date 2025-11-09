package com.mineinabyss.plugin

import com.mineinabyss.components.curse.AscensionEffect
import com.mineinabyss.features.AbyssContext
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.Guilds
import com.mineinabyss.features.guilds.database.Players
import com.mineinabyss.features.helpers.Placeholders
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.features.tools.ToolsFeature
import com.mineinabyss.features.tutorial.TutorialFeature
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.di.Idofront
import com.mineinabyss.idofront.features.featureManager
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.module.Module

context(plugin: Plugin)
fun Idofront.pluginModule(block: Module.() -> Unit) {

}

class MineInAbyssPlugin : JavaPlugin() {
//    override fun getKoin(): Koin = koinApplication {
//    }.koin

    val featureManager = featureManager {
        globalModule {
            single<AbyssContext> { abyss }
        }

        withMainCommand("mineinabyss", "mia", description = "The main command for Mine in Abyss")

        install(*abyss.config.features.toTypedArray())
    }


    override fun onLoad() {
        gearyPaper.configure {
            install(createAddon("MineInAbyss", configuration = {
                autoscan(
                    classLoader,
                    "com.mineinabyss.features",
                    "com.mineinabyss.components",
                    "com.mineinabyss.mineinabyss.core"
                ) {
                    components()
                    subClassesOf<AscensionEffect>()
                }
            }))
        }

        DI.add<AbyssContext>(AbyssContext(this))
        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
        transaction(abyss.db) {
            SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, GuildMessageQueue, LootedChests)
        }

        if (abyss.isPlaceholderApiLoaded) {
            Placeholders().register()
        }
        featureManager.load()
    }

    override fun onEnable() {
        featureManager.enable()
    }

    override fun onDisable() {
        featureManager.disable()
    }
}
