package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.TradeSelectEvent

class GuideBookListener : Listener {

    @EventHandler
    fun TradeSelectEvent.onTrade() {
        if (index == 0) {
            //TODO revert page to previous
        } else {
            //TODO Find the current
        }
        view.title(":guidebook_page$index:".miniMsg())
    }
}