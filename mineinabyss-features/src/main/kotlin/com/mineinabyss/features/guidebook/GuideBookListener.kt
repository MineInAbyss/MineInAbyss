package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.event.player.PlayerTradeEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.TradeSelectEvent

class GuideBookListener : Listener {

    @EventHandler
    fun TradeSelectEvent.onTrade() {
        view.title(":guidebook_page$index:".miniMsg())
    }
}