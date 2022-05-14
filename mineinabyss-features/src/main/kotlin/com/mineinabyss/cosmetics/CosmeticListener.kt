package com.mineinabyss.cosmetics

import com.mineinabyss.components.cosmetics.cosmetics
import com.mineinabyss.helpers.equipCosmeticBackPack
import com.mineinabyss.helpers.getCosmeticBackpack
import com.mineinabyss.helpers.unequipCosmeticBackpack
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.serialization.toSerializable
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

class CosmeticListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.equipBackpack() {
        if (player.isSneaking && rightClicked) {
            if (player.getCosmeticBackpack().itemStack.type == Material.AIR) {
                val type = item?.type.toString().replace("_SHULKER_BOX", "").lowercase()
                val meta = (item?.itemMeta as? BlockStateMeta)?.blockState as? ShulkerBox ?: return

                isCancelled = true
                meta.inventory.filterNotNull().forEach {
                    player.cosmetics.backpackContent?.add(it.toSerializable())
                }
                item?.subtract(1)
                if (player.cosmetics.cosmeticBackpack == null)
                    player.equipCosmeticBackPack("default_$type")
                else
                    player.equipCosmeticBackPack(player.cosmetics.cosmeticBackpack!!)
            }
            else {
                val inv = player.inventory
                val item = ItemStack(player.getCosmeticBackpack().itemStack)
                val bsm = (item.itemMeta as? BlockStateMeta) ?: return
                val box = bsm.blockState as? ShulkerBox ?: return

                isCancelled = true
                player.cosmetics.backpackContent?.forEach {
                    box.inventory.addItem(it.toItemStack())
                }
                bsm.blockState = box
                item.itemMeta = bsm
                if (inv.itemInMainHand.type == Material.AIR)
                    inv.setItemInMainHand(item)
                else if (inv.itemInOffHand.type == Material.AIR)
                    inv.setItemInOffHand(item)
                else if (inv.firstEmpty() != -1)
                    inv.setItem(inv.firstEmpty(), item)
                else return
                player.unequipCosmeticBackpack()
                player.cosmetics.backpackContent?.clear()
            }
        }
    }
}
