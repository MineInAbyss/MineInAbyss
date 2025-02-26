package com.mineinabyss.plugin

import com.mineinabyss.features.AbyssContext
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildMessagesTable
import com.mineinabyss.features.guilds.data.tables.GuildsTable
import com.mineinabyss.features.guilds.data.tables.GuildMembersTable
import com.mineinabyss.features.helpers.Placeholders
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureManager
import com.mineinabyss.idofront.plugin.actions
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal val featureManager by DI.observe<AbyssFeatureManager>()

// very meta
class AbyssFeatureManager(plugin: JavaPlugin) : FeatureManager<AbyssContext>(plugin, { AbyssContext(plugin) }) {
    override fun FeatureDSL.enable() = actions(logger) {
        transaction(abyss.db) {
            //addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(GuildsTable, GuildMembersTable, GuildJoinRequestsTable, GuildMessagesTable, LootedChests)
        }

        if (abyss.isPlaceholderApiLoaded) {
            Placeholders().register()
        }
    }
}
