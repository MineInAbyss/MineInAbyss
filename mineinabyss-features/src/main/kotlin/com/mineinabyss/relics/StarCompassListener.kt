package com.mineinabyss.relics

import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.CompassMeta
import kotlin.time.Duration.Companion.seconds

@AutoScan
class StarCompassSystem: TickingSystem(interval = 2.seconds) {
    private val TargetScope.player by get<Player>()

    override fun TargetScope.tick() {
        val compass = player.inventory.contents.firstOrNull {
            if (it == null) return@firstOrNull false
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