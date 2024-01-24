package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareGrindstoneEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta

class DoToggleStarCompassHud : GearyListener() {
    private val Pointers.item by get<ItemStack>().on(target)
    private val Pointers.action by family { has<ToggleStarCompassHud>() }.on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val player = target.entity.parent?.get<Player>() ?: return
        if (target.entity.has<ShowStarCompassHud>()) {
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.editItemMeta<CompassMeta> {
                lodestone = player.location.section?.centerLocation
                isLodestoneTracked = false
            }
            target.entity.remove<ShowStarCompassHud>()
        } else {
            target.entity.setPersisting(ShowStarCompassHud())
            item.removeEnchantment(Enchantment.ARROW_INFINITE)
            item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
            item.editItemMeta<CompassMeta> {
                lodestone = null
                isLodestoneTracked = false
            }
        }
    }
}

class StarCompassBukkitListener : Listener {
    @EventHandler
    fun PrepareGrindstoneEvent.onGrindStarCompass() {
        if (this.result?.itemMeta?.persistentDataContainer?.decodePrefabs()
                ?.contains(PrefabKey.of("mineinabyss:star_compass")) == true
        )
            result = null
    }
}
