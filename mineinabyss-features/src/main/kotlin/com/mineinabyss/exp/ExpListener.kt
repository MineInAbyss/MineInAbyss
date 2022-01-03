package com.mineinabyss.exp

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.mineinabyss.core.AbyssContext
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent

class ExpListener : Listener {
    @EventHandler
    fun PlayerExpChangeEvent.onPlayerGainEXP() {
        val (player, amount) = this
        if (amount <= 0) return
        AbyssContext.econ?.depositPlayer(player, amount.toDouble())
        player.playerData.addExp(amount.toDouble())
    }
}
