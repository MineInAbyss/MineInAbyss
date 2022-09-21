package com.mineinabyss.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthBanker
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.helpers.changeHudState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent

class OrthBankingListener(private val feature: OrthBankingFeature) : Listener {

    @EventHandler
    fun PlayerJoinEvent.showBalance() = player.changeHudState(feature.balanceHudId, player.playerData.showPlayerBalance)

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractBanker() {
        rightClicked.toGearyOrNull()?.get<OrthBanker>() ?: return
        //guiy { BankMenu(player) } // Comment out until we use a GUI for banking
    }
}
