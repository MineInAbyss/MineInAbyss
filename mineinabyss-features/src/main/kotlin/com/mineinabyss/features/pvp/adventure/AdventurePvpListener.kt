package com.mineinabyss.features.pvp.adventure

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.pvp.PvpPrompt
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdventurePvpListener : Listener {
    @EventHandler
    fun PlayerDescendEvent.promptPvpSelect() {
        if (!player.playerData.showPvpPrompt || fromSection != Features.layers.config.hubSection) return
        guiy { PvpPrompt(player) }
    }
}
