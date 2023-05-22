package com.mineinabyss.features.cosmetics

import com.destroystokyo.paper.MaterialTags
import com.hibiscusmc.hmccosmetics.api.PlayerCosmeticEquipEvent
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.mineinabyss.components.cosmetics.BackpackStorage
import com.mineinabyss.components.cosmetics.CosmeticComponent
import com.mineinabyss.components.cosmetics.cosmeticComponent
import com.mineinabyss.features.helpers.equipCosmeticBackPack
import com.mineinabyss.features.helpers.unequipCosmeticBackpack
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class CosmeticListener(private val feature: CosmeticsFeature) : Listener {

    // Hide HMCCosmetics backpack equip if player isn't wearing a backpack
    // Cancel HMCCosmetics backpack equip if player isn't wearing a backpack
    /*@EventHandler
    fun CosmeticChangeEvent.onEquipBackpack() {
        val player = (user as? User ?: return).player ?: return
        if (cosmeticItem.type != ArmorItem.Type.BACKPACK || player.toGeary().has<Backpack>()) return
        player.toGeary().setPersisting(Cosmetics(cosmeticBackpack = cosmeticItem.id))
    }*/

    @EventHandler
    fun PlayerCosmeticEquipEvent.onEquipBackpack() {
        if (cosmetic.slot != CosmeticSlot.BACKPACK) return
        val geary = user.player.toGearyOrNull() ?: return
        geary.setPersisting(CosmeticComponent(user.player.cosmeticComponent.gesture, cosmetic.id))
        if (!geary.has<BackpackStorage>()) {
            user.player.error("Equip a backpack (Shift + Right Click) to show the cosmetic")
            //user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN)
            isCancelled = true
        }
        //TODO Send message to player informing them that they arent "wearing" a backpack thus no cosmetic?
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEvent.equipBackpack() {
        if (hand != EquipmentSlot.HAND || !player.isSneaking || !rightClicked) return
        val itemInHand = player.inventory.itemInMainHand

        player.toGearyOrNull()?.let {
            when {
                it.has<BackpackStorage>() && (itemInHand.type.isAir || player.inventory.firstEmpty() != -1) -> {
                    val backpack = it.get<BackpackStorage>()!!.backpack
                    player.inventory.addItem(backpack)
                    it.remove<BackpackStorage>()
                    player.unequipCosmeticBackpack()
                }

                !it.has<BackpackStorage>() && itemInHand.type in MaterialTags.SHULKER_BOXES.values -> {
                    val backpackId = Cosmetics.getCosmetic(player.cosmeticComponent.cosmeticBackpack)?.id
                    if (backpackId == null)
                        it.setPersisting(CosmeticComponent(player.cosmeticComponent.gesture, feature.defaultBackpack))

                    it.setPersisting(BackpackStorage(itemInHand))
                    player.equipCosmeticBackPack(backpackId ?: feature.defaultBackpack)
                    player.inventory.setItemInMainHand(null)
                }

                else -> return
            }
        }

    }
}
