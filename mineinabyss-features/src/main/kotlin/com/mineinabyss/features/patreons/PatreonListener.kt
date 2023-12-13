package com.mineinabyss.features.patreons

import com.mineinabyss.chatty.components.chattyNickname
import com.mineinabyss.components.players.Patreon
import com.mineinabyss.features.helpers.luckpermGroups
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
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
        val patreon = player.toGeary().get<Patreon>()
        // Remove perks from old patreons
        if (permGroups.any { it.startsWith("patreon") })
            player.toGeary().setPersisting(patreon?.copy(tier = if ("patreonplus" in permGroups) 2 else 1) ?: Patreon())
        else player.removePatreonPerks()
    }

    private fun Player.removePatreonPerks() {
        val patreon = toGeary().get<Patreon>() ?: return
        toGeary().setPersisting(patreon.copy(tier = 0))
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user $name meta clear prefix")
        this.chattyNickname = null
    }
}
