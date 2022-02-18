package com.mineinabyss.relics

import com.mineinabyss.components.layer.Layer
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.core.layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta

@Serializable
@SerialName("mineinabyss:locate_abyss_center")
class LocateAbyssCenter()

class StarCompassListener : GearyListener() {

    @EventHandler
    fun PlayerAscendEvent.updateStarCompass() {
        val compass = player.inventory.itemInMainHand
        if (compass.type != Material.COMPASS) return
        broadcast("t")
        compass.toGearyOrNull(player)?.get<StarCompass>() ?: return

        val section = player.location.section ?: return
        val center = section.region.center

        compass.editItemMeta { this as CompassMeta
            this.lodestone = Location(player.world, center.x.toDouble(), center.y.toDouble(), center.z.toDouble())
            this.isLodestoneTracked = false
        }
    }

    private val TargetScope.player by get<Player>()
    private val SourceScope.starCompass by get<StarCompass>()

    init {
        event.has<LocateAbyssCenter>()
    }

    @Handler
    fun TargetScope.showCenter(source: SourceScope) {
        val section = player.location.section
        val layer: Layer? = section?.layer
        val compass: ItemStack = player.inventory.contents.firstOrNull() { it.type == Material.COMPASS } ?: return
        if (compass.toGearyOrNull(player)?.getComponents()?.contains(source.starCompass) == false) return

        if (layer?.name != null) {
            val sectionCenter = section.region.center
            val center =
            compass.editItemMeta { this as CompassMeta
                lodestone = Location(player.world, sectionCenter.x.toDouble(), sectionCenter.y.toDouble(), sectionCenter.z.toDouble())
                isLodestoneTracked = false
            }

        }
    }
}