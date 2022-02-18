package com.mineinabyss.relics

import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.meta.CompassMeta

class StarCompassListener : Listener {

    @EventHandler
    fun PlayerMoveEvent.initiateStarCompass() {
        val compass = player.inventory.contents.firstOrNull {
            it.type = Material.COMPASS
            it.toGearyOrNull(player)?.has<StarCompass>() ?: return@firstOrNull false
        } ?: return
        val section = player.location.section ?: return
        val center = section.region.center

        if ((compass.itemMeta as CompassMeta).hasLodestone()) return
        compass.editItemMeta { this as CompassMeta
            this.lodestone = Location(player.world, center.x.toDouble(), center.y.toDouble(), center.z.toDouble())
            this.isLodestoneTracked = false
        }
    }

    @EventHandler
    fun PlayerAscendEvent.updateStarCompass() {
        val compass = player.inventory.contents.firstOrNull {
            it.type = Material.COMPASS
            it.toGearyOrNull(player)?.has<StarCompass>() ?: return@firstOrNull false
        } ?: return
        val section = player.location.section ?: return
        val center = section.region.center

        compass.editItemMeta { this as CompassMeta
            this.lodestone = Location(player.world, center.x.toDouble(), center.y.toDouble(), center.z.toDouble())
            this.isLodestoneTracked = false
        }
    }

    @EventHandler
    fun PlayerDescendEvent.updateStarCompass() {
        val compass = player.inventory.contents.firstOrNull {
            it.type = Material.COMPASS
            it.toGearyOrNull(player)?.has<StarCompass>() ?: return@firstOrNull false
        } ?: return
        val section = player.location.section ?: return
        val center = section.region.center

        compass.toGearyOrNull(player)?.get<StarCompass>() ?: return
        compass.editItemMeta { this as CompassMeta
            this.lodestone = Location(player.world, center.x.toDouble(), center.y.toDouble(), center.z.toDouble())
            this.isLodestoneTracked = false
        }
    }
}