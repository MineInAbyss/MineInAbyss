package com.mineinabyss.features.guilds.listeners

import com.mineinabyss.features.guilds.guildParty
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GuildDungeonListener : Listener {

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        player.guildParty()?.removePlayer(player)
    }
}