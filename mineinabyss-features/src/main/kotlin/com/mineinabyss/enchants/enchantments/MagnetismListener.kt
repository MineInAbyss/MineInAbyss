package com.mineinabyss.enchants.enchantments

import com.mineinabyss.enchants.CustomEnchants.MAGNETISM
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent


class MagnetismListener : Listener {
    @EventHandler
    fun BlockBreakEvent.onBlockBreak() {

        val item = player.inventory.itemInMainHand

        if (item.containsEnchantment(MAGNETISM)){

            val loc = block.location

            mineInAbyss.schedule {
                waitFor(1)

                val entities = player.world.getNearbyEntities(loc, 4.0, 4.0, 4.0)

                for (entity in entities) {
                    if (entity is Item) {
                        val item: Item = entity
                        val stack = player.inventory.addItem(item.itemStack)
                        if (stack[0] == null){
                            entity.remove()
                        }
                    }
                }
            }
        }
    }
}
