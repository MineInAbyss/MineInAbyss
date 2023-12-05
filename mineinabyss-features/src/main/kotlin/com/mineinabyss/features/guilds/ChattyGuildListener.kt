package com.mineinabyss.features.guilds

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.helpers.getDefaultChat
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.guildChatId
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
        val channelData = player.toGeary().get<ChannelData>()?.withChannelVerified() ?: return
        val channelId = channelData.channelId

        if ((!channelId.startsWith(player.getGuildName()) && channelId.endsWith(guildChannelId)) || channelId !in chatty.config.channels.keys) {
            abyss.plugin.launch {
                player.toGeary().setPersisting(channelData.copy(channelId = getDefaultChat().key))
            }
            return
        }

        if (channelId != player.guildChatId()) return

        viewers().clear()
        viewers().addAll(Bukkit.getOnlinePlayers().filter {
            it.getGuildName() == player.getGuildName()
        })
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        val channelData = player.toGeary().get<ChannelData>()?.withChannelVerified() ?: return
        if (channelData.channelId.endsWith(guildChannelId)) {
            abyss.plugin.launch {
                player.toGeary()
                    .setPersisting(channelData.copy(channelId = player.guildChatId().takeIf { it.isNotBlank() }
                        ?: getDefaultChat().key))
            }
        }
    }
}
