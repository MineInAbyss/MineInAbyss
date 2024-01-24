package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.family.family
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
        if (target.entity.has<ShowDepthMeterHud>()) {
            target.entity.remove<ShowDepthMeterHud>()
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        } else {
            target.entity.setPersisting(ShowDepthMeterHud())
            item.removeEnchantment(Enchantment.ARROW_INFINITE)
            item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    }
}
