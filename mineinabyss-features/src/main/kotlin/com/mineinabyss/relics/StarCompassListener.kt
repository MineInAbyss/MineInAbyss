package com.mineinabyss.relics

import com.mineinabyss.components.helpers.HideBossBarCompass
import com.mineinabyss.components.helpers.PlayerCompassBar
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.get
import com.mineinabyss.helpers.bossbarCompass
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.looty.tracking.toGearyOrNull
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.CompassMeta
import kotlin.time.Duration.Companion.seconds

class StarCompassSystem : TickingSystem(interval = 0.1.seconds) {
    private val TargetScope.starCompass by get<StarCompass>()

    override fun TargetScope.tick() {
        val player = entity.parent?.get<Player>() ?: return
        val compassList =
            player.inventory.contents?.filter {
                it != null && it.toGearyOrNull(player)?.has<StarCompass>() == true
            } ?: return

        val playerBar = player.toGeary().getOrSetPersisting { PlayerCompassBar() }
        val sectionCenter = player.location.section?.region?.center

        if (sectionCenter != null) starCompass.compassLocation =
            Location(player.world, sectionCenter.x.toDouble(), 0.0, sectionCenter.z.toDouble())
        else starCompass.compassLocation = null

        // Let player toggle between having a bossbar-compass or item compass
        compassList.forEach { compass ->
            if (player.toGeary().has<HideBossBarCompass>()) {
                compass?.type = Material.COMPASS
                compass?.editItemMeta {
                    this as CompassMeta
                    lodestone = starCompass.compassLocation
                    isLodestoneTracked = false
                }
                player.hideBossBar(playerBar.compassBar)
            } else {
                compass?.type = Material.PAPER
                player.bossbarCompass(starCompass.compassLocation, playerBar.compassBar)
            }
        }
    }
}

/*
* To not search every players inventory every tick above, it is limited to only those with the item.
* This class is meant to remove the bar from anyone who might still have it but not the item
*/
@AutoScan
class RemoveStarCompassBar : TickingSystem(interval = 2.seconds) {
    override fun TargetScope.tick() {
        val player = entity.get<Player>() ?: return
        val playerBar = player.toGeary().get<PlayerCompassBar>() ?: return

        player.hideBossBar(playerBar.compassBar)
        player.toGeary().remove<PlayerCompassBar>()
    }
}
