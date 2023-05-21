package com.mineinabyss.pvp.adventure

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.mineinabyss.core.abyss
import com.mineinabyss.pvp.PvpPrompt
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdventurePvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.promptPvpSelect() {
        if (!player.playerData.showPvpPrompt || fromSection != abyss.config.hubSection) return
        guiy { PvpPrompt(player) }
    }
}
