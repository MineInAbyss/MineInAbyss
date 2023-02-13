package com.mineinabyss.patreons

import com.mineinabyss.components.players.Patreon
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.luckpermGroups
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PatreonListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.addPatreonComponent() {
        val isPatreon = (player.luckpermGroups.contains("patreon") || player.luckpermGroups.contains("patreonplus"))
        val gearyPlayer = player.toGeary()

        // Remove perks from old patreons
        if (!isPatreon && gearyPlayer.has<Patreon>()) {
            player.removePatreonPerks()
            gearyPlayer.remove<Patreon>()
        } else {
            val patreon = gearyPlayer.getOrSetPersisting { Patreon() }
            if (player.luckpermGroups.any { it == "patreonplus" }) patreon.tier = 2
        }
    }
}

private fun Player.removePatreonPerks() {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user $name meta clear prefix")
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nick $name off")
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "nametagedit player $name clear")
}
