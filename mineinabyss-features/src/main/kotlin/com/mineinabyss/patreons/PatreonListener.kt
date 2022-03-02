package com.mineinabyss.patreons

import com.mineinabyss.components.players.Patreon
import com.mineinabyss.geary.papermc.access.toGeary
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PatreonListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.addPatreonComponent() {
        if (!player.hasPermission("group.patreon") && !player.hasPermission("group.patreonplus")) return
        if (!player.toGeary().has<Patreon>()) player.toGeary().setPersisting(Patreon)
        val patreon = player.toGeary().get<Patreon>() ?: return

        if (player.hasPermission("group.patreonplus")) patreon.tier = 2
        else patreon.tier = 1
    }

    @EventHandler
    fun PlayerQuitEvent.saveLastMonth() {
        val patreon = player.toGeary().get<Patreon>() ?: return
    }
}