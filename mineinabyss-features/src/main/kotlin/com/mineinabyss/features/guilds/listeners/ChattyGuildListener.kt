package com.mineinabyss.features.guilds.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.events.ChattyPlayerChatEvent
import com.mineinabyss.chatty.helpers.defaultChannel
import com.mineinabyss.components.guilds.SpyOnGuildChat
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.getGuildName
import com.mineinabyss.features.guilds.extensions.guildChat
import com.mineinabyss.features.guilds.extensions.guildChatId
import com.mineinabyss.features.guilds.guildChannelId
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ChattyGuildListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun ChattyPlayerChatEvent.onGuildChat() {
        val guildName = player.getGuildName()
        if (channel.key != player.guildChatId()) return

        viewers.clear()
        viewers.addAll(player.server.onlinePlayers.filter { it.getGuildName() == guildName || it.toGeary().has<SpyOnGuildChat>() })
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        val channelData = player.toGeary().get<ChannelData>()?.withChannelVerified() ?: return
        val channelId = player.guildChat()?.key ?: defaultChannel().key
        if (!channelData.channelId.endsWith(guildChannelId)) return
        abyss.plugin.launch { player.toGeary().setPersisting(channelData.copy(channelId = channelId)) }
    }
}
