package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class DoToggleDepthHud : GearyListener() {
    private val Pointers.item by get<ItemStack>().on(target)
    private val Pointers.action by family { has<ToggleDepthHud>() }.on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val item = item
        if (target.entity.has<ShowDepthMeterHud>()) {
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            target.entity.remove<ShowDepthMeterHud>()
        } else {
            item.removeEnchantment(Enchantment.ARROW_INFINITE)
            item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
            target.entity.setPersisting(ShowDepthMeterHud())
        }
        target.entity.encodeComponentsTo(item)
    }
}
