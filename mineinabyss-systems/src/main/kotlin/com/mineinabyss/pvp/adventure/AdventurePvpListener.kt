package com.mineinabyss.pvp.adventure

import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.components.playerData
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.pvp.PvpPrompt
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdventurePvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.promptPvpSelect() {
        val data = player.playerData

        if (fromSection == MIAConfig.data.hubSection && data.showPvpPrompt) guiy { PvpPrompt(player) }
    }

//    @EventHandler
//    fun PlayerAscendEvent.checkMessageToggle() {
//        val data = player.playerData
//        // If player hasn't toggled message off, set them as undecided
//        if (toSection == MIAConfig.data.hubSection && data.showPvpPrompt) data.pvpUndecided = true
//    }
}