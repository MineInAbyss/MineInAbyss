package com.mineinabyss.npc.shopkeeping

import com.mineinabyss.components.npc.shopkeeping.ShopKeeper
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.npc.shopkeeping.menu.ShopMainMenu
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class ShopKeepingListener : Listener {

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractShopKeeper() {
        val shopkeeper = rightClicked.toGearyOrNull()?.get<ShopKeeper>() ?: return
        guiy {
            ShopMainMenu(player, shopkeeper)
            player.playSound(player, Sound.ENTITY_VILLAGER_TRADE, 1f, 1f)
        }
    }
}
