package com.mineinabyss.features.core

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType

class CoreListener : Listener {

    @EventHandler
    fun InventoryOpenEvent.onInvOpen() {
        if (inventory.type != InventoryType.CHEST) return
        view.title = ":shift_-8::vanilla_chest_${inventory.size / 9}::shift_-167:${view.originalTitle}"
    }
}
