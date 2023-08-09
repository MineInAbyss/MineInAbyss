package com.mineinabyss.features.core

import io.papermc.paper.event.player.PlayerFailMoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CoreListener : Listener {

    @EventHandler
    fun PlayerFailMoveEvent.onMoveWrongly() {
        when (failReason) {
            PlayerFailMoveEvent.FailReason.MOVED_TOO_QUICKLY -> logWarning = false
            PlayerFailMoveEvent.FailReason.MOVED_WRONGLY -> logWarning = false
            else -> {}
        }
    }
}
