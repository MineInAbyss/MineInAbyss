package com.mineinabyss.features.okibotravel

import com.mineinabyss.features.okibotravel.menu.OkiboMapMenu
import com.mineinabyss.idofront.messaging.error
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot

class OkiboTravelListener(private val config: OkiboTravelConfig, private val menu: OkiboMapMenu) : Listener {
    @EventHandler
    fun NexoFurnitureInteractEvent.onClickNoticeboard() {
        if (hand != EquipmentSlot.HAND || player.isSneaking) return
        if (mechanic.itemID !in config.noticeboards) return
        isCancelled = true

        val station = config.stationNear(baseEntity.location) ?: return player.error("This noticeboard is not near an okibo station!")
        menu.open(player, station, baseEntity, mechanic.properties.scale, mechanic.properties.translation)
    }
}
