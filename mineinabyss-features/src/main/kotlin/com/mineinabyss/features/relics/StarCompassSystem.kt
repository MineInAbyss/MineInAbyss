package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
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
        player.toGeary().let {
            if (it.has<ShowStarCompassHud>()) {
                item.type = Material.COMPASS
                item.editItemMeta {
                    this as CompassMeta
                    lodestone = player.toGeary().get<ShowStarCompassHud>()?.lastSection?.centerLocation
                    isLodestoneTracked = false
                    itemFlags.add(ItemFlag.HIDE_ENCHANTS)
                }
                it.remove<ShowStarCompassHud>()
            }
            else {
                item.type = Material.COMPASS
                item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
                it.setPersisting(ShowStarCompassHud(player.location.section))
            }
        }
    }
}

class ToggleStarCompassHud : RepeatingSystem(5.ticks) {
    private val TargetScope.player by get<Player>()

    override fun TargetScope.tick() {
        val starCompasses = player.inventory.withIndex().filter { player.inventory.toGeary()?.get(it.index)?.has<StarCompass>() == true }.mapNotNull { it.value }
        when {
            starCompasses.any { it.itemMeta is CompassMeta } -> player.toGeary().remove<ShowStarCompassHud>()
            else -> player.toGeary().setPersisting(ShowStarCompassHud(player.location.section))
        }
    }
}
