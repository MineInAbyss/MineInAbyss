package com.mineinabyss.features.relics

import com.mineinabyss.components.relics.ShowStarCompassHud
import com.mineinabyss.deeperworld.world.section.centerLocation
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.LodestoneTracker
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


fun Geary.toggleStarCompassHudAction() = observe<ToggleStarCompassHud>()
    .exec(query<ItemStack>()) { (item) ->
        val player = entity.parent?.get<Player>() ?: return@exec
        if (entity.has<ShowStarCompassHud>()) {
            item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            val lodestone = LodestoneTracker.lodestoneTracker(player.location.section?.centerLocation, false)
            item.setData(DataComponentTypes.LODESTONE_TRACKER, lodestone)
            entity.remove<ShowStarCompassHud>()
        } else {
            entity.setPersisting(ShowStarCompassHud())
            item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            val lodestone = LodestoneTracker.lodestoneTracker(null, false)
            item.setData(DataComponentTypes.LODESTONE_TRACKER, lodestone)
        }
        entity.encodeComponentsTo(item)
    }
