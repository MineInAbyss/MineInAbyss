package com.mineinabyss.npc.orthbanking

import com.mineinabyss.components.npc.orthbanking.OrthBanker
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.helpers.toggleHud
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent

class OrthBankingListener(private val feature: OrthBankingFeature) : Listener {

    @EventHandler
    fun PlayerJoinEvent.showBalance() = player.toggleHud(feature.balanceHudId, player.playerData.showPlayerBalance)

    @EventHandler
    fun PlayerMoveEvent.onEnterWater() {
        if (!hasChangedBlock()) return
        if (player.isInWaterOrBubbleColumn && player.playerData.showPlayerBalance) {
            player.toggleHud(feature.balanceHudId, false)
            player.sendActionBar(Component.empty())
        }
        else if (!player.isInWaterOrBubbleColumn && !player.playerData.showPlayerBalance)
            player.toggleHud(feature.balanceHudId, true)
    }

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractBanker() {
        rightClicked.toGearyOrNull()?.get<OrthBanker>() ?: return
        //guiy { BankMenu(player) } // Comment out until we use a GUI for banking
    }
}
