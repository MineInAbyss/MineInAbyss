package com.mineinabyss.fasttravel.okiboline

import com.mineinabyss.components.fasttravel.okiboline.OkiboLine
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class OkiboLineListener : Listener {

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractOkiboLine() {
        val entity = rightClicked.toGearyOrNull()?.get<OkiboLine>() ?: return
        //guiy {  }
    }
}
