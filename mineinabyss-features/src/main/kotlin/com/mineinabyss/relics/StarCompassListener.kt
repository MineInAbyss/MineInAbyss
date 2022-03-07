package com.mineinabyss.relics

import com.mineinabyss.components.helpers.HideBossBarCompass
import com.mineinabyss.components.helpers.PlayerCompassBar
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.autoscan.AutoScan
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.bossbarCompass
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.CompassMeta
import kotlin.time.Duration.Companion.seconds

@AutoScan
class StarCompassSystem : TickingSystem(interval = 0.1.seconds) {
    private val TargetScope.player by get<Player>()

    override fun TargetScope.tick() {
        val compass =
        entity.get<Player>()?.inventory?.contents?.firstOrNull() {
            if (it == null) return@firstOrNull false
            it.toGearyOrNull(player)?.has<StarCompass>() ?: return@firstOrNull false
        } ?: return
        val starCompass = compass.toGearyOrNull(player)?.get<StarCompass>() ?: return
        val playerBar = player.toGeary().getOrSetPersisting { PlayerCompassBar() }
        val hideCompass = player.toGeary().has<HideBossBarCompass>()
        val sectionCenter = player.location.section?.region?.center

        if (sectionCenter != null) starCompass.compassLocation = Location(player.world, sectionCenter.x.toDouble(), 0.0, sectionCenter.z.toDouble())

        // Let player toggle between having a bossbar-compass or item compass
        if (!hideCompass) {
            compass.type = Material.PAPER
            player.bossbarCompass(starCompass.compassLocation!!, playerBar.compassBar)
        }
        else {
            compass.type = Material.COMPASS
            compass.editItemMeta { this as CompassMeta
                lodestone = starCompass.compassLocation
                isLodestoneTracked = false
            }
            player.hideBossBar(playerBar.compassBar)
        }
    }
}