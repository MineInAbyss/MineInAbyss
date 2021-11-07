package com.mineinabyss.pvp.adventure

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.core.MIAConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdventurePvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.onEnterAbyss() {
        val data = player.playerData

        if (fromSection == MIAConfig.data.hubSection && data.showPvpPrompt) {
            player.performCommand("mia pvp")

        }
    }

    @EventHandler
    fun PlayerAscendEvent.checkMessageToggle() {
        val data = player.playerData
        // If player hasn't toggled message off, set them as undecided
        if (toSection == MIAConfig.data.hubSection && data.showPvpPrompt) data.pvpUndecided = true
    }
}