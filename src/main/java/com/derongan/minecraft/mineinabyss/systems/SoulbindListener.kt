package com.derongan.minecraft.mineinabyss.systems

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.mineinabyss.components.Orthbound
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
import com.mineinabyss.geary.minecraft.components.Soulbound
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
import com.mineinabyss.looty.tracking.toGearyFromUUIDOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

object SoulbindListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        if (toSection != MIAConfig.data.hubSection) return
        player.inventory.contents.filterNotNull().forEach {
            val item = it.toGearyFromUUIDOrNull() ?: return
            item.get<Orthbound>() ?: return@forEach
            item.setPersisting(Soulbound(owner = player.uniqueId))
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
