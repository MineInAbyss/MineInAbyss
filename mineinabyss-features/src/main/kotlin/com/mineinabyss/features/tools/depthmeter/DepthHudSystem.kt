package com.mineinabyss.features.tools.depthmeter

import com.mineinabyss.components.tools.DepthMeter
import com.mineinabyss.components.tools.ShowDepthMeterHud
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

@Serializable
@SerialName("mineinabyss:toggle_depth_hud")
class ToggleDepthHud

class ToggleDepthHudSystem : GearyListener() {
    private val Pointers.player by get<Player>().on(target)
    private val Pointers.depthMeter by get<DepthMeter>().on(source)
    private val Pointers.hasDepth by family { has<ToggleDepthHud>() }.on(event)

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

class DepthHudSystem : RepeatingSystem(5.ticks) {
    private val Pointer.player by get<Player>()

    override fun Pointer.tick() {
        if (!player.isConnected) return
        val depthMeters = player.inventory.withIndex().filter { player.inventory.toGeary()?.get(it.index)?.has<DepthMeter>() == true }.mapNotNull { it.value }
        when {
            depthMeters.any { Enchantment.ARROW_INFINITE !in it.enchantments } ->
                player.toGeary().add<ShowDepthMeterHud>()
            else -> player.toGeary().remove<ShowDepthMeterHud>()
        }
    }
}
