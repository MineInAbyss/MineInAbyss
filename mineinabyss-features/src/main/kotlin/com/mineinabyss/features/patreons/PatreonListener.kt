package com.mineinabyss.features.patreons

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PatreonListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.syncPatreonPerks() {
        player.migrateLegacyPatreonPrefix()
        player.revokeLapsedPatreonPerks()
    }
}
