package com.mineinabyss.patreons

import com.mineinabyss.components.players.Patreon
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.getGroups
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PatreonListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.addPatreonComponent() {
        val isPatreon = (player.getGroups().contains("patreon") || player.getGroups().contains("patreonplus"))

        if (!isPatreon) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user ${player.name} meta clear prefix")
            return
        }
        if (!player.toGeary().has<Patreon>()) player.toGeary().getOrSetPersisting { Patreon() }
        val patreon = player.toGeary().get<Patreon>() ?: return

        if (player.getGroups().any { it -> it == "patreonplus" }) patreon.tier = 2
    }
}
