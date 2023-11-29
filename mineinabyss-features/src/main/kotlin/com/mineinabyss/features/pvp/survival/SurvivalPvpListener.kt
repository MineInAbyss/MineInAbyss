package com.mineinabyss.features.pvp.survival

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.idofront.messaging.error
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SurvivalPvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.onEnterPvPLayer() {
        if (fromSection.layer?.hasPvpDefault == false && toSection.layer?.hasPvpDefault == true && !player.playerData.pvpStatus) {
            player.error("PVP is always enabled below this point.")
        }
    }
}
