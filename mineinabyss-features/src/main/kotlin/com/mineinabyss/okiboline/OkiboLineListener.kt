package com.mineinabyss.okiboline

import com.mineinabyss.components.fasttravel.okiboline.OkiboLine
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.okiboline.menus.OkiboLineMenu
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class OkiboLineListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun PlayerInteractEntityEvent.onInteractOkiboLine() {
        rightClicked.toGearyOrNull()?.get<OkiboLine>() ?: return
        rightClicked !is Player || return // Since players also use component for some stuff
        guiy { OkiboLineMenu(player) }
        isCancelled = true // Bypass other potential interaction events like mounting
    }
}
