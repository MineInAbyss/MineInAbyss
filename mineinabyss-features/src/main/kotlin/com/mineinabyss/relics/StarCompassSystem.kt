package com.mineinabyss.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
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
import com.mineinabyss.helpers.getSectionCenter
import com.mineinabyss.helpers.toggleHud
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.CompassMeta

@Serializable
@SerialName("mineinabyss:toggle_starcompass_hud")
class ToggleDepthHud

class ToggleStarCompassHudSystem : GearyListener() {
    private val TargetScope.player by get<Player>()
    private val SourceScope.starCompass by get<StarCompass>()
    private val EventScope.hasStarCompass by family { has<ToggleDepthHud>() }

    @Handler
    fun TargetScope.toggleDepth(source: SourceScope) {
        val item = player.inventory.itemInMainHand
        if (player.toGeary().has<ShowStarCompassHud>()) {
            item.type = Material.COMPASS
            item.editItemMeta {
                this as CompassMeta
                lodestone = player.toGeary().get<ShowStarCompassHud>()?.lastSection?.getSectionCenter()
                isLodestoneTracked = false
                itemFlags.add(ItemFlag.HIDE_ENCHANTS)
            }
            player.toGeary().remove<ShowStarCompassHud>()
        }
        else {
            item.type = Material.PAPER
            item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
            player.toGeary().setPersisting(ShowStarCompassHud(player.location.section))
        }
    }
}

class StarCompassSystem(private val feature: RelicsFeature) : RepeatingSystem(interval = 2.ticks) {
    private val TargetScope.starCompass by get<StarCompass>()

    override fun TargetScope.tick() {
        val player = entity.parent?.get<Player>() ?: return
        player.toggleHud(feature.starcompassHudId, player.toGeary().has<ShowStarCompassHud>())
    }
}

class RemoveStarCompassBar(private val feature: RelicsFeature) : GearyListener() {
    private val TargetScope.starCompass by get<StarCompass>()
    private val EventScope.removed by family { has<EntityRemoved>() }

    @Handler
    fun TargetScope.removeBar() {
        val parent = entity.parent ?: return
        val player = parent.get<Player>() ?: return

        player.toggleHud(feature.starcompassHudId, false)
        parent.remove<ShowStarCompassHud>()
    }
}
