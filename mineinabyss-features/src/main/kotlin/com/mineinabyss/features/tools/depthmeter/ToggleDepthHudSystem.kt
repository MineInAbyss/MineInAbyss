package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.DepthMeter
import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class ToggleDepthHudSystem : GearyListener() {
    private val Pointers.player by get<Player>().on(target)
    private val Pointers.hasDepth by family { has<ToggleDepthHud>() }.on(source)

    override fun Pointers.handle() {
        val item = player.inventory.itemInMainHand
        if (player.toGeary().has<ShowDepthMeterHud>()) {
            player.toGeary().remove<ShowDepthMeterHud>()
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        else {
            player.toGeary().setPersisting(ShowDepthMeterHud())
            item.removeEnchantment(Enchantment.ARROW_INFINITE)
            item.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    }
}
