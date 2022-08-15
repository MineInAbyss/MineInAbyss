package com.mineinabyss.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.npc.shopkeeping.menu.ShopMenu
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class ShopKeepingListener : Listener {

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractShopKeeper() {
        val shopkeeper = rightClicked.toGearyOrNull()?.get<ShopKeeper>() ?: return
        guiy { ShopMenu(player, shopkeeper) }
    }
}
