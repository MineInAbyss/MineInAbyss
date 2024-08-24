package com.mineinabyss.features.guidebook

import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.TradeSelectEvent

class GuideBookListener : Listener {

    @EventHandler
    fun TradeSelectEvent.onTrade() {
        val player = whoClicked as Player
        val guideBookPage = GuideBookPage.findGuideBookPage(player) ?: return
        guideBookPage.buttons[index]?.action?.invoke(view)
    }
}