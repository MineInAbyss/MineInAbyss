package com.mineinabyss.relics

import com.mineinabyss.components.helpers.HideBossBarCompass
import com.mineinabyss.components.helpers.PlayerCompassBar
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.helpers.bossbarCompass
import com.mineinabyss.idofront.items.editItemMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import kotlin.time.Duration.Companion.seconds

@Serializable
@SerialName("mineinabyss:toggle_starcompass_hud")
class ToggleDepthHud

class ToggleStarCompassHudSystem : GearyListener() {
    private val TargetScope.player by get<Player>()
    private val SourceScope.starCompass by get<StarCompass>()
    private val EventScope.hasStarCompass by family { has<ToggleDepthHud>() }

    @Handler
    fun TargetScope.toggleDepth(source: SourceScope) {
        if (player.toGeary().has<HideBossBarCompass>())
            player.toGeary().remove<HideBossBarCompass>()
        else player.toGeary().setPersisting(HideBossBarCompass())
    }
}

class StarCompassSystem : RepeatingSystem(interval = 0.1.seconds) {
    private val TargetScope.starCompass by get<StarCompass>()
    private val TargetScope.item by get<ItemStack>()

    override fun TargetScope.tick() {
        val player = entity.parent?.get<Player>() ?: return
        val playerBar = player.toGeary().getOrSetPersisting { PlayerCompassBar() }
        val center = player.location.section?.region?.center

        if (center != null)
            starCompass.compassLocation = Location(player.world, center.x.toDouble(), 0.0, center.z.toDouble())
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

class RemoveStarCompassBar : GearyListener() {
    private val TargetScope.starCompass by get<StarCompass>()
    private val EventScope.removed by family { has<EntityRemoved>() }

    @Handler
    fun TargetScope.removeBar() {
        val parent = entity.parent ?: return
        val playerBar = parent.get<PlayerCompassBar>() ?: return
        val player = parent.get<Player>() ?: return

        player.hideBossBar(playerBar.compassBar)
        parent.remove<PlayerCompassBar>()
    }
}
