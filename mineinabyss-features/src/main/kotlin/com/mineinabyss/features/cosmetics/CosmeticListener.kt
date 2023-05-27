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
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class CosmeticListener(private val feature: CosmeticsFeature) : Listener {

    @EventHandler
    fun PlayerCosmeticEquipEvent.onEquipBackpack() {
        if (cosmetic.slot != CosmeticSlot.BACKPACK) return
        user.player.toGeary().let {
            it.setPersisting(CosmeticComponent(user.player.cosmeticComponent.gesture, cosmetic.id))
            if (!it.has<BackpackStorage>()) {
                user.player.error("Equip a backpack (Shift + Right Click) to show the cosmetic")
                //user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN)
                isCancelled = true
            }
        }
    }

    @EventHandler
    fun PlayerInteractEvent.equipBackpack() {
        if (!player.isSneaking || !rightClicked) return
        val (item, hand) = (item ?: ItemStack(Material.AIR)) to (hand ?: return)

        player.toGearyOrNull()?.let { gearyPlayer ->
            when {
                gearyPlayer.has<BackpackStorage>() && item.type.isAir && player.inventory.firstEmpty() != -1 -> {
                    player.inventory.addItem(gearyPlayer.get<BackpackStorage>()!!.backpack)
                    gearyPlayer.remove<BackpackStorage>()
                    player.unequipCosmeticBackpack()
                }

                !gearyPlayer.has<BackpackStorage>() && !item.type.isAir &&MaterialTags.SHULKER_BOXES.isTagged(item) -> {
                    val backpackId = Cosmetics.getCosmetic(player.cosmeticComponent.cosmeticBackpack)?.id
                    if (backpackId == null)
                        gearyPlayer.setPersisting(CosmeticComponent(player.cosmeticComponent.gesture, feature.defaultBackpack))

                    gearyPlayer.setPersisting(BackpackStorage(item))
                    player.equipCosmeticBackPack(backpackId ?: feature.defaultBackpack)
                    player.inventory.getItem(hand).subtract()
                }

                else -> return
            }
            isCancelled = true
        }

    }
}
