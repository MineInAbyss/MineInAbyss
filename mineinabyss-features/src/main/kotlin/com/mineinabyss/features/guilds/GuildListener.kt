package com.mineinabyss.features.guilds

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.chatty.listeners.RendererExtension
import com.mineinabyss.components.guilds.GuildMaster
import com.mineinabyss.features.guilds.database.GuildMessageQueue
import com.mineinabyss.features.guilds.database.GuildMessageQueue.content
import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.extensions.getGuildChatId
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.getGuildRank
import com.mineinabyss.features.guilds.extensions.hasGuild
import com.mineinabyss.features.guilds.menus.GuildMainMenu
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.abyss
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import nl.rutgerkok.blocklocker.group.GroupSystem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.seconds

class GuildListener(private val feature: GuildFeature) : Listener {
    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractGuildMaster() {
        rightClicked.toGearyOrNull()?.get<GuildMaster>() ?: return
        guiy { GuildMainMenu(player, feature, true) }
    }

    @EventHandler
    suspend fun PlayerJoinEvent.onJoin() {
        delay(1.seconds)
        withContext(abyss.plugin.asyncDispatcher) {
            transaction(abyss.db) {
                GuildMessageQueue.select { GuildMessageQueue.playerUUID eq player.uniqueId }.forEach {
                    player.info(it[content].miniMsg())
                }
                GuildMessageQueue.deleteWhere { playerUUID eq player.uniqueId }
            }
        }
    }
}

class GuildContainerSystem : GroupSystem() {

    override fun isInGroup(player: Player, guildName: String): Boolean {
        val name = player.getGuildName().replace(" ", "_")
        return name.equals(guildName, true) && player.hasGuild()
    }

    override fun isGroupLeader(player: Player, groupName: String): Boolean {
        val guild = player.hasGuild()
        if (!guild) return false

        return player.getGuildRank() == GuildRank.OWNER
    }
}

class ChattyGuildListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun AsyncChatEvent.onGuildChat() {
        val channelId = player.chattyData.channelId

        if ((!channelId.startsWith(player.getGuildName()) && channelId.endsWith(guildChannelId)) || channelId !in chatty.config.channels.keys) {
            player.chattyData.channelId = getDefaultChat().key
            return
        }
        if (player.chattyData.channelId != player.getGuildChatId()) return

        viewers().clear()
        viewers().addAll(Bukkit.getOnlinePlayers().filter {
            it.getGuildName() == player.getGuildName() && it != player
        })

        if (chatty.config.chat.disableChatSigning) {
            viewers().forEach { a -> RendererExtension.render(player, player.displayName(), message(), a) }
            viewers().clear()
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        if (player.chattyData.channelId.endsWith(guildChannelId)) {
            player.chattyData.channelId =
                player.getGuildChatId().takeIf { it.isNotBlank() } ?: getDefaultChat().key
        }
    }
}
