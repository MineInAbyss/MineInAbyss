package com.mineinabyss.cosmetics

import com.hibiscusmc.hmccosmetics.api.PlayerCosmeticEquipEvent
import com.hibiscusmc.hmccosmetics.api.PlayerCosmeticRemoveEvent
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.mineinabyss.components.cosmetics.CosmeticComponent
import com.mineinabyss.components.cosmetics.cosmeticComponent
import com.mineinabyss.components.players.Backpack
import com.mineinabyss.geary.papermc.access.toGeary
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
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

class CosmeticListener : Listener {

    // Cancel HMCCosmetics backpack equip if player isn't wearing a backpack
    @EventHandler
    fun PlayerCosmeticEquipEvent.onEquipBackpack() {
        user.player.toGeary().setPersisting(
            CosmeticComponent(
                user.player.cosmeticComponent.gesture,
                user.getCosmetic(CosmeticSlot.BACKPACK).id
            )
        )
    }

    @EventHandler
    fun PlayerCosmeticRemoveEvent.onRemoveBackpack() {

    }

    @EventHandler
    fun PlayerInteractEvent.equipBackpack() {
        if (hand != EquipmentSlot.HAND || !player.isSneaking || !rightClicked) return
        // Put backpack on and store backpack-inv
        val backpackType = player.getCosmeticBackpack()?.item?.type ?: Material.AIR
        if (backpackType == Material.AIR) {
            // Get the color of the players backpack, or non-colored, from the material
            val item = player.inventory.itemInMainHand
            val i = item.type.toString().lowercase()
            val type = if (i.startsWith("shulker")) "yellow" else i.replace("_shulker_box", "")
            val meta = (item.itemMeta as? BlockStateMeta)?.blockState as? ShulkerBox ?: return
            val backpack = player.toGeary().getOrSetPersisting { Backpack() }

            isCancelled = true
            meta.inventory.filterNotNull().forEach {
                backpack.backpackContent.add(it.toSerializable())
            }
            item.subtract()

            // Use default color backpack or custom one if specified so by the component
            //player.toGeary().setPersisting(CosmeticComponent(player.cosmeticComponent.gesture, "default_$type"))
            player.equipCosmeticBackPack(player.cosmeticComponent.cosmeticBackpack)
        }

        // Remove backpack and refill backpack with inventory
        else {
            // Create a new itemstack of the cosmetic backpack
            val inv = player.inventory
            val item = ItemStack(backpackType)
            val bsm = (item.itemMeta as? BlockStateMeta) ?: return
            val box = bsm.blockState as? ShulkerBox ?: return
            isCancelled = true

            // Add all the items on the component back into the backpack
            player.toGeary().get<Backpack>()?.backpackContent?.forEach {
                box.inventory.addItem(it.toItemStack())
            }

            bsm.blockState = box
            item.itemMeta = bsm

            when {
                inv.itemInMainHand.type == Material.AIR -> inv.setItemInMainHand(item)
                inv.itemInOffHand.type == Material.AIR -> inv.setItemInOffHand(item)
                inv.firstEmpty() != -1 -> inv.addItem(item)
                else -> return
            }

            val i = item.type.toString().lowercase()
            val type = if (i.startsWith("shulker")) "yellow" else i.replace("_shulker_box", "")

            // Unequip the backpack from the player
            //player.toGeary().setPersisting(CosmeticComponent(player.cosmeticComponent.gesture, "default_$type"))
            player.unequipCosmeticBackpack()
        }
    }
}
