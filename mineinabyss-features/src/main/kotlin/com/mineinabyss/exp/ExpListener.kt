package com.mineinabyss.exp

import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent

class ExpListener : Listener {
    @EventHandler
    fun PlayerExpChangeEvent.onPlayerGainEXP() {
        if (amount <= 0) return
        abyss.econ?.depositPlayer(player, amount.toDouble())
        player.playerData.addExp(amount.toDouble())
    }
}
