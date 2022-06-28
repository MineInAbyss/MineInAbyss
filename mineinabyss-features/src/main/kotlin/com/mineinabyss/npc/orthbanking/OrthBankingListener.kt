package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthBanker
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.npc.orthbanking.ui.BankMenu
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent

class OrthBankingListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.showBalance() {
        if (player.playerData.showPlayerBalance) player.updateBalance()
    }

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractBanker() {
        rightClicked.toGearyOrNull()?.get<OrthBanker>() ?: return
        guiy { BankMenu(player) }
    }
}
