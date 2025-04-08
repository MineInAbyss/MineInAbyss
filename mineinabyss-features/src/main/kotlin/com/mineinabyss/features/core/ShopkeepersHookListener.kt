package com.mineinabyss.features.core

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.nisovin.shopkeepers.api.events.UpdateItemEvent
import com.nisovin.shopkeepers.api.util.UnmodifiableItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ShopkeepersHookListener : Listener {

    val itemProvider = gearyPaper.worldManager.global.getAddon(ItemTracking).itemProvider

    @EventHandler
    fun UpdateItemEvent.onStartup() {
        val prefab = itemProvider.deserializeItemStackToEntity(item.itemMeta?.persistentDataContainer ?: return)?.get<PrefabKey>() ?: return
        item = UnmodifiableItemStack.ofNonNull(itemProvider.serializePrefabToItemStack(prefab))
    }
}