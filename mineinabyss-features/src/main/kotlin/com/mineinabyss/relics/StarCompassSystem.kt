package com.mineinabyss.relics

import com.mineinabyss.components.helpers.HideBossBarCompass
import com.mineinabyss.components.helpers.PlayerCompassBar
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.helpers.bossbarCompass
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import kotlin.time.Duration.Companion.seconds

class StarCompassSystem : TickingSystem(interval = 0.1.seconds) {
    private val TargetScope.starCompass by get<StarCompass>()
    private val TargetScope.item by get<ItemStack>()

    override fun TargetScope.tick() {
        val player = entity.parent?.get<Player>() ?: return
        val playerBar = player.toGeary().getOrSetPersisting { PlayerCompassBar() }
        val sectionCenter = player.location.section?.region?.center

        if (sectionCenter != null) starCompass.compassLocation =
            Location(player.world, sectionCenter.x.toDouble(), 0.0, sectionCenter.z.toDouble())
        else starCompass.compassLocation = null

        // Let player toggle between having a bossbar-compass or item compass
        if (player.toGeary().has<HideBossBarCompass>()) {
            item.type = Material.COMPASS
            item.editItemMeta {
                this as CompassMeta
                lodestone = starCompass.compassLocation
                isLodestoneTracked = false
            }
            player.hideBossBar(playerBar.compassBar)
        } else {
            item.type = Material.PAPER
            player.bossbarCompass(starCompass.compassLocation, playerBar.compassBar)
        }
    }
}

//TODO fix when component removal events get implemented
@AutoScan
class RemoveStarCompassBar : GearyListener() {
    private val TargetScope.starCompass by get<StarCompass>()
    private val EventScope.removed by family { has<EntityRemoved>() }

    @Handler
    fun TargetScope.removeBar() {
        val parent = entity.parent ?: return
        val player = parent.get<Player>() ?: return
        val playerBar = parent.get<PlayerCompassBar>() ?: return

        player.hideBossBar(playerBar.compassBar)
        parent.remove<PlayerCompassBar>()
    }
}
