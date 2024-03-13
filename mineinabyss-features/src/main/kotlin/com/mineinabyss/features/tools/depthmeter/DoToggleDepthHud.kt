package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

fun GearyModule.createToggleDepthHudAction() = listener(object : ListenerQuery() {
    val item by get<ItemStack>()
    override fun ensure() = source { has<ToggleDepthHud>() }

}).exec {
    val item = item
    if (entity.has<ShowDepthMeterHud>()) {
        item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        entity.remove<ShowDepthMeterHud>()
    } else {
        item.removeEnchantment(Enchantment.ARROW_INFINITE)
        item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        entity.setPersisting(ShowDepthMeterHud())
    }
    entity.encodeComponentsTo(item)

}
