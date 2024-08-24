package com.mineinabyss.features.guidebook

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.entities.title
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.TradeSelectEvent
import org.bukkit.inventory.view.MerchantView

class GuideBookListener : Listener {

    @EventHandler
    fun TradeSelectEvent.onTrade() {
        val player = whoClicked as Player
        val guideBookPage = GuideBookPage.findGuideBookPage(player) ?: return
        guideBookPage.buttons[index]?.action?.invoke(view)
    }

    @EventHandler
    fun InventoryCloseEvent.onCloseGuideBook() {
        if (view is MerchantView) GuideBookHelpers.showInventory(player as Player)
    }

    @EventHandler
    fun InventoryClickEvent.onClickGuideBook() {
        if (view !is MerchantView) return
        isCancelled = true
        // Client updates inventory on click so we schedule this to run on next tick
        Bukkit.getScheduler().runTask(abyss.plugin, Runnable {
            GuideBookHelpers.hideInventory(whoClicked as Player)
        })
    }
}