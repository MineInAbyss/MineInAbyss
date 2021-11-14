package com.mineinabyss.orthbanking

import com.mineinabyss.components.npc.OrthBanking.OrthBanker
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class OrthBankingListener : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.onBanker() {
        val gearyEntity = rightClicked.toGearyOrNull() ?: return
        val banker = gearyEntity.get<OrthBanker>() ?: return

        guiy { BankerMenu(player) }
    }
}