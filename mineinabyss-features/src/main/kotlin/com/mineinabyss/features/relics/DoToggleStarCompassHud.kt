package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta


fun Geary.toggleStarCompassHudAction() = observe<ToggleStarCompassHud>()
    .exec(query<ItemStack>()) { (item) ->
        val player = entity.parent?.get<Player>() ?: return@exec
        if (entity.has<ShowStarCompassHud>()) {
            item.editItemMeta<CompassMeta> {
                setEnchantmentGlintOverride(true)
                lodestone = player.location.section?.centerLocation
                isLodestoneTracked = false
            }
            entity.remove<ShowStarCompassHud>()
        } else {
            entity.setPersisting(ShowStarCompassHud())
            item.editItemMeta<CompassMeta> {
                setEnchantmentGlintOverride(false)
                lodestone = null
                isLodestoneTracked = false
            }
        }
        entity.encodeComponentsTo(item)
    }
