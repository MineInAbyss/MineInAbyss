package com.mineinabyss.fasttravel.okiboline

import com.mineinabyss.components.fasttravel.okiboline.OkiboLine
import com.mineinabyss.fasttravel.okiboline.menus.OkiboLineScreen
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class OkiboLineListener : Listener {

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractOkiboLine() {
        rightClicked.toGearyOrNull()?.get<OkiboLine>() ?: return
        rightClicked !is Player || return
        guiy { OkiboLineScreen(player) }
    }
}
