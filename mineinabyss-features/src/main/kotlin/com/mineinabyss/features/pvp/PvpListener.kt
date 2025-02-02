package com.mineinabyss.features.pvp

import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.idofront.messaging.error
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.onEnterPvPLayer() {
        if (fromSection.layer?.hasPvpDefault == false && toSection.layer?.hasPvpDefault == true && player.playerDataOrNull?.pvpStatus != true) {
            player.error("PVP is always enabled below this point.")
        }
    }
}
