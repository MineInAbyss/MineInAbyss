package com.mineinabyss.enchants.enchantments

import com.mineinabyss.components.soulbound.Orthbound
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.geary.papermc.components.Soulbound
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import com.mineinabyss.mineinabyss.core.MIAConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class SoulBoundListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        if (toSection != MIAConfig.data.hubSection) return
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyFromUUIDOrNull() ?: return
            item.get<Orthbound>() ?: return@forEach
            item.setPersisting(Soulbound(player.uniqueId))
            item.encodeComponentsTo(it)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerDeathEvent.death() {
        val player = entity
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyFromUUIDOrNull() ?: return

            if (item.get<Soulbound>()?.owner == player.uniqueId) {
                if (drops.contains(it)) {
                    drops -= it
                    itemsToKeep += it
                }
            }
            return@forEach
        }
    }
}
