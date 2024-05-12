package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.query
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

fun GearyModule.createToggleDepthHudAction() = geary.observe<ToggleDepthHud>().exec(query<ItemStack>()) { (item) ->
    val item = item
    if (entity.has<ShowDepthMeterHud>()) {
        item.addUnsafeEnchantment(Enchantment.INFINITY, 1)
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        entity.remove<ShowDepthMeterHud>()
    } else {
        item.removeEnchantment(Enchantment.INFINITY)
        item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        entity.setPersisting(ShowDepthMeterHud())
    }
    entity.encodeComponentsTo(item)

}
