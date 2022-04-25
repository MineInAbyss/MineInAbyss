package com.mineinabyss.enchants.enchantments

import com.mineinabyss.enchants.CustomEnchants.Magnetism
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

        if (item.containsEnchantment(Magnetism)){

            val loc = block.location

            mineInAbyss.schedule {
                waitFor(1)

                val entities = player.world.getNearbyEntities(loc, 4.0, 4.0, 4.0)

                for (entitie in entities) {
                    if (entitie is Item) {
                        val item: Item = entitie
                        val fullInv = player.inventory.firstEmpty() == -1
                        if (!fullInv){
                            player.inventory.addItem(item.itemStack)
                            entitie.remove()
                        }
                        else{
                            for (slot in player.inventory.contents) {
                                if (slot.asOne() == item.itemStack.asOne() && slot.amount != 64) {
                                    player.inventory.addItem(item.itemStack)
                                    entitie.remove()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
//full inventory
//anvil
