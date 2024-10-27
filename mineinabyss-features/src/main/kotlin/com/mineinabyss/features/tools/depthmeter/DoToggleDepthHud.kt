package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack

fun Geary.createToggleDepthHudAction() = observe<ToggleDepthHud>().exec(query<ItemStack>()) { (item) ->
    if (entity.has<ShowDepthMeterHud>()) {
        item.editItemMeta { setEnchantmentGlintOverride(true) }
        entity.remove<ShowDepthMeterHud>()
    } else {
        item.editItemMeta { setEnchantmentGlintOverride(false) }
        entity.setPersisting(ShowDepthMeterHud())
    }
    entity.encodeComponentsTo(item)

}
