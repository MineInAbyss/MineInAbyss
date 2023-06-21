package com.mineinabyss.patreons

import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.components.players.patreon
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.luckpermGroups
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PatreonListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerJoinEvent.addPatreonComponent() {
        val permGroups = player.luckpermGroups
        player.toGeary().let { gearyPlayer ->
            // Remove perks from old patreons
            if (permGroups.any { it.startsWith("patreon") })
                gearyPlayer.setPersisting(player.patreon?.copy(tier = if ("patreonplus" in permGroups) 2 else 1) ?: Patreon())
            else player.removePatreonPerks()
        }
    }

    private fun Player.removePatreonPerks() {
        toGeary().setPersisting(patreon?.copy(tier = 0) ?: return)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user $name meta clear prefix")
        this.chattyNickname = null
    }
}
