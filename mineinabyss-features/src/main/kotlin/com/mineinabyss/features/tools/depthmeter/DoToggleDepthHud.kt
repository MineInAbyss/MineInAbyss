package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.ItemStack

fun Geary.createToggleDepthHudAction() = observe<ToggleDepthHud>().exec(query<ItemStack>()) { (item) ->
    if (entity.has<ShowDepthMeterHud>()) {
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        entity.remove<ShowDepthMeterHud>()
    } else {
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
        entity.setPersisting(ShowDepthMeterHud())
    }
    entity.encodeComponentsTo(item)

}
