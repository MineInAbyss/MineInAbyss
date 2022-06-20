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
        val gearyPlayer = player.toGeary()

        // Remove perks from old patreons
        if (!isPatreon && player.toGeary().has<Patreon>()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user ${player.name} meta clear prefix")
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick ${player.name} off")
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nametagedit player ${player.name} clear")
            gearyPlayer.remove<Patreon>()
            return
        }

        val patreon = gearyPlayer.getOrSetPersisting { Patreon() }
        if (player.getGroups().any { it -> it == "patreonplus" }) patreon.tier = 2
    }
}
