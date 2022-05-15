package com.mineinabyss.enchants.enchantments

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.enchants.CustomEnchants.MAGNETISM
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.mineinabyss.core.mineInAbyss
import kotlinx.coroutines.delay
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent


class MagnetismListener : Listener {
    @EventHandler
    fun BlockBreakEvent.onBlockBreak() {

        val item = player.inventory.itemInMainHand

        if (item.containsEnchantment(MAGNETISM)) {

            val loc = block.location

            mineInAbyss.launch {
                delay(1.ticks)

                val entities = player.world.getNearbyEntities(loc, 4.0, 4.0, 4.0)

                for (entity in entities) {
                    if (entity is Item) {
                        val remaining = player.inventory.addItem(entity.itemStack)[0]
                        if(remaining == null) entity.remove()
                        else entity.itemStack = remaining
                    }
                }
            }
        }
    }
}
