package com.mineinabyss.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthBanker
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.helpers.updateBalance
import com.mineinabyss.orthbanking.ui.BankMenu
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent

class OrthBankingListener : Listener {

    @EventHandler
    fun PlayerJoinEvent.showBalance() {
        if (player.playerData.showPlayerBalance) player.updateBalance()
    }

    @EventHandler
    fun PlayerMoveEvent.onEnterWater() {
        if (!hasChangedBlock()) return
        if (player.isInWaterOrBubbleColumn && player.playerData.showPlayerBalance)
            player.playerData.showPlayerBalance = false
        else if (!player.isInWaterOrBubbleColumn && !player.playerData.showPlayerBalance) {
            player.playerData.showPlayerBalance = true
            player.updateBalance()
        }
    }

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractBanker() {
        rightClicked.toGearyOrNull()?.get<OrthBanker>() ?: return
        guiy { BankMenu(player) }
    }
}
