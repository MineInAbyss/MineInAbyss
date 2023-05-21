package com.mineinabyss.enchants.enchantments

import com.mineinabyss.components.soulbound.Orthbound
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.geary.papermc.components.Soulbound
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.core.MIAConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class SoulBoundListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        if (toSection != abyss.config.hubSection) return
        player.inventory.contents?.filterNotNull()?.forEach {
            val item = it.toGearyFromUUIDOrNull() ?: return
            item.get<Orthbound>() ?: return@forEach
            item.setPersisting(Soulbound(player.uniqueId))
            item.encodeComponentsTo(it)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerDeathEvent.onDeath() {
        player.inventory.contents?.filterNotNull()?.forEach { item ->
            if (item.toGearyOrNull(player)?.get<Soulbound>()?.owner == player.uniqueId) {
                if (item in drops) {
                    drops.remove(item)
                    itemsToKeep.add(item)
                }
            }
        }
    }
}
