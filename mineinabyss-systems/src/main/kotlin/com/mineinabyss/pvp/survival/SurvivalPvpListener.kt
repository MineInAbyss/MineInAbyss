package com.mineinabyss.pvp.survival

import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.layer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PromptSurvivalPvpSelect : Listener {
    @EventHandler
    fun PlayerDescendEvent.onEnterPvPLayer() {
        val data = player.playerData
        if (toSection.layer?.hasPvPDefault == true && !data.pvpStatus) {
            player.error("PVP is always enabled below this point.")
        }
    }
}