package com.mineinabyss.features.enchants.enchantments

import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class SoulBoundListener : Listener {
    @EventHandler
    fun PlayerAscendEvent.soulbindItems() {
        /*if (toSection != abyss.config.hubSection) return
        player.inventory.contents?.filterNotNull()?.forEach {
            val item = it.itemMeta.persistentDataContainer ?: return@forEach
            item.decode<Orthbound>() ?: return@forEach
            item.encode(Soulbound(player.uniqueId))
        }*/
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun PlayerDeathEvent.onDeath() {
        /*player.inventory.contents?.filterNotNull()?.forEachIndexed { slot, item ->
            if (player.inventory.toGeary()?.get(slot)?.get<Soulbound>()?.owner == player.uniqueId) {
                if (item !in drops) return@forEachIndexed
                drops.remove(item)
                itemsToKeep.add(item)
            }
        }*/
    }
}
