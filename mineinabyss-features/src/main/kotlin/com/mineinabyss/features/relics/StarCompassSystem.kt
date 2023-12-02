package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.components.relics.StarCompass
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.datastore.decodeComponents
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.CompassMeta

@Serializable
@SerialName("mineinabyss:toggle_starcompass_hud")
class ToggleDepthHud

class ToggleStarCompassHudSystem : GearyListener() {
    private val Pointers.player by get<Player>().on(target)
    private val Pointers.starCompass by get<StarCompass>().on(source)
    private val Pointers.hasStarCompass by family { has<ToggleDepthHud>() }.on(event)

    override fun Pointers.handle() {
        val item = player.inventory.itemInMainHand
        item.type = Material.COMPASS
        player.toGeary().let {
            if (it.has<ShowStarCompassHud>()) {
                item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
                item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                item.editItemMeta<CompassMeta> {
                    lodestone = player.toGeary().get<ShowStarCompassHud>()?.lastSection?.centerLocation
                    isLodestoneTracked = false
                }
                it.remove<ShowStarCompassHud>()
            }
            else {
                it.setPersisting(ShowStarCompassHud(player.location.section))
                item.removeEnchantment(Enchantment.ARROW_INFINITE)
                item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
                item.editItemMeta<CompassMeta> {
                    lodestone = null
                    isLodestoneTracked = false
                }
            }
        }
    }
}

class ToggleStarCompassHud : RepeatingSystem(5.ticks) {
    private val Pointer.player by get<Player>()

    override fun Pointer.tick() {
        if (!player.isConnected) return
        val starCompasses = player.inventory.withIndex().filter { player.inventory.toGeary()?.get(it.index)?.has<StarCompass>() == true }.mapNotNull { it.value }
        when {
            starCompasses.any { it.hasItemFlag(ItemFlag.HIDE_ENCHANTS) } -> player.toGeary().remove<ShowStarCompassHud>()
            else -> player.toGeary().setPersisting(ShowStarCompassHud(player.location.section))
        }
    }
}

class StarCompassBukkitListener : Listener {
    @EventHandler
    fun PrepareGrindstoneEvent.onGrindStarCompass() {
        if (this.result?.itemMeta?.persistentDataContainer?.decodePrefabs()?.contains(PrefabKey.of("mineinabyss:star_compass")) == true)
            result = null
    }
}
