package com.mineinabyss.features.enchants.enchantments

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.enchants.CustomEnchants.MAGNETISM
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.delay
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent


class MagnetismListener : Listener {
    @EventHandler
    fun BlockBreakEvent.onBlockBreak() {
        if (MAGNETISM in player.inventory.itemInMainHand.enchantments) {
            abyss.plugin.launch {
                delay(1.ticks)

                player.world.getNearbyEntities(block.location, 4.0, 4.0, 4.0).forEach { entity ->
                    if (entity is Item) {
                        val remaining = player.inventory.addItem(entity.itemStack)[0]
                        if (remaining == null) entity.remove()
                        else entity.itemStack = remaining
                    }
                }
            }
        }
    }
}
