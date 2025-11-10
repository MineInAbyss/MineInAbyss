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
import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.datastore.PrefabNamespaceMigrations
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class MineInAbyssPlugin : JavaPlugin() {
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
    }

    override fun onEnable() {
        DI.add<AbyssContext>(AbyssContext(this))

        PrefabNamespaceMigrations.migrations += listOf("looty" to "mineinabyss", "mobzy" to "mineinabyss")
        transaction(abyss.db) {
            SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, GuildMessageQueue, LootedChests)
        }

        if (abyss.isPlaceholderApiLoaded) {
            Placeholders().register()
        }
        abyss.featureManager.load()
        abyss.featureManager.enable()
    }

    override fun onDisable() {
        abyss.featureManager.disable()
    }
}
