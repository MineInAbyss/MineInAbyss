package com.mineinabyss.plugin

import com.mineinabyss.features.AbyssContext
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildJoinQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.Guilds
import com.mineinabyss.features.guilds.database.Players
import com.mineinabyss.features.helpers.Placeholders
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureManager
import com.mineinabyss.idofront.plugin.actions
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

internal val featureManager by DI.observe<AbyssFeatureManager>()

// very meta
class AbyssFeatureManager(val plugin: JavaPlugin) : FeatureManager<AbyssContext>({ AbyssContext(plugin) }) {
    override fun FeatureDSL.enable() = actions {
        transaction(abyss.db) {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(Guilds, Players, GuildJoinQueue, GuildMessageQueue, LootedChests)
        }

        if (abyss.isPlaceholderApiLoaded) {
            Placeholders().register()
        }
    }
}
