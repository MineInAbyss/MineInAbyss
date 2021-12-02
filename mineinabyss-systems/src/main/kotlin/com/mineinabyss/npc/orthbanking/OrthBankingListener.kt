package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.npc.OrthBanking.OrthBanker
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.mineinabyss.updateBalance
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
        val entity = rightClicked.toGearyOrNull() ?: return
        entity.get<OrthBanker>() ?: return

        guiy { BankMenu(player) }
    }
}