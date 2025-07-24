package com.mineinabyss.features.guilds.listeners

import com.mineinabyss.components.guilds.GuildMaster
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue.content
import com.mineinabyss.features.guilds.menus.GuildMainMenu
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Duration.Companion.seconds

class GuildListener : Listener {
    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractGuildMaster() {
        rightClicked.toGearyOrNull()?.get<GuildMaster>() ?: return
        guiy { GuildMainMenu(player, true) }
    }

    private val databaseDispatcher = Dispatchers.IO.limitedParallelism(1)

    @EventHandler
    suspend fun PlayerJoinEvent.onJoin() {
        delay(1.seconds)
        withContext(databaseDispatcher) {
            transaction(abyss.db) {
                GuildMessageQueue.selectAll().where { GuildMessageQueue.playerUUID eq player.uniqueId }.forEach {
                    player.info(it[content].miniMsg())
                }
                GuildMessageQueue.deleteWhere { playerUUID eq player.uniqueId }
            }
        }
    }
}
