package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.playerData
import com.mineinabyss.mineinabyss.updateBalance
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class OrthBankingListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.showBalance() {
        if (player.playerData.showPlayerBalance) player.updateBalance()
    }

}