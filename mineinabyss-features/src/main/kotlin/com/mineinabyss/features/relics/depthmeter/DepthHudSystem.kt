package com.mineinabyss.features.relics.depthmeter

import com.mineinabyss.components.relics.DepthMeter
import com.mineinabyss.components.relics.ShowDepthMeterHud
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
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
    private val TargetScope.player by get<Player>()
    private val SourceScope.depthMeter by get<DepthMeter>()
    private val EventScope.hasDepth by family { has<ToggleDepthHud>() }

    @Handler
    fun TargetScope.toggleDepth(source: SourceScope) {
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
    private val TargetScope.player by get<Player>()

    override fun TargetScope.tick() {
        when {
            player.inventory.withIndex().any { player.inventory.toGeary()?.get(it.index)?.has<DepthMeter>() == true } ->
                player.toGeary().add<ShowDepthMeterHud>()
            else -> player.toGeary().remove<ShowDepthMeterHud>()
        }
    }
}
