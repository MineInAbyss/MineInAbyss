package com.mineinabyss.cosmetics

import com.mineinabyss.components.cosmetics.cosmetics
import com.mineinabyss.components.players.Backpack
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.helpers.equipCosmeticBackPack
import com.mineinabyss.helpers.getCosmeticBackpack
import com.mineinabyss.helpers.unequipCosmeticBackpack
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.serialization.toSerializable
import io.github.fisher2911.hmccosmetics.api.event.CosmeticChangeEvent
import io.github.fisher2911.hmccosmetics.gui.ArmorItem
import io.github.fisher2911.hmccosmetics.user.User
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

class CosmeticListener : Listener {

    // Cancel HMCCosmetics backpack equip if player isn't wearing a backpack
    @EventHandler
    fun CosmeticChangeEvent.onEquipBackpack() {
        val player = (user as? User ?: return).player
        if (cosmeticItem.type == ArmorItem.Type.BACKPACK && !player!!.toGeary().has<Backpack>()) {
            player.cosmetics.cosmeticBackpack = cosmeticItem.id
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerInteractEvent.equipBackpack() {
        if (player.isSneaking && rightClicked) {
            if (player.getCosmeticBackpack().itemStack.type == Material.AIR) {
                val i = item?.type.toString().lowercase()
                val type = if (!i.startsWith("shulker")) i.replace("_shulker_box", "") else i
                val meta = (item?.itemMeta as? BlockStateMeta)?.blockState as? ShulkerBox ?: return

                isCancelled = true
                val backpack = player.toGeary().getOrSetPersisting { Backpack() }
                meta.inventory.filterNotNull().forEach {
                    backpack.backpackContent?.add(it.toSerializable())
                }
                item?.subtract(1)
                if (player.cosmetics.cosmeticBackpack == null) {
                    if (type.contains("shulker")) player.equipCosmeticBackPack("default_yellow")
                    else player.equipCosmeticBackPack("default_$type")
                }
                else player.equipCosmeticBackPack(player.cosmetics.cosmeticBackpack!!)
            }
            else {
                val inv = player.inventory
                val item = ItemStack(player.getCosmeticBackpack().itemStack)
                val bsm = (item.itemMeta as? BlockStateMeta) ?: return
                val box = bsm.blockState as? ShulkerBox ?: return

                isCancelled = true
                player.toGeary().get<Backpack>()?.backpackContent?.forEach {
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
                player.toGeary().get<Backpack>()?.backpackContent?.clear()
                player.toGeary().remove<Backpack>()
            }
        }
    }
}
