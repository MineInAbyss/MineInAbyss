package com.mineinabyss.features.guilds

import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.chattyData
import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.chatty.listeners.RendererExtension
import com.mineinabyss.features.guilds.extensions.getGuildChatId
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ChattyGuildListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun AsyncChatEvent.onGuildChat() {
        val channelId = player.chattyData.channelId

        if ((!channelId.startsWith(player.getGuildName()) && channelId.endsWith(guildChannelId)) || channelId !in chatty.config.channels.keys) {
            player.toGeary().setPersisting(player.chattyData.copy(channelId = getDefaultChat().key))
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
            player.toGeary().setPersisting(player.chattyData.copy(channelId = player.getGuildChatId().takeIf { it.isNotBlank() } ?: getDefaultChat().key))
        }
    }
}
