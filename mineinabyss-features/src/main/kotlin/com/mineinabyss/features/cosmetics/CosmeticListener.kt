package com.mineinabyss.features.cosmetics

import com.destroystokyo.paper.MaterialTags
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticPostEquipEvent
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType
import com.mineinabyss.components.cosmetics.BackpackStorage
import com.mineinabyss.components.cosmetics.CosmeticComponent
import com.mineinabyss.components.cosmetics.cosmeticComponent
import com.mineinabyss.features.helpers.equipCosmeticBackPack
import com.mineinabyss.features.helpers.isInventoryFull
import com.mineinabyss.features.helpers.unequipCosmeticBackpack
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class CosmeticListener(private val feature: CosmeticsFeature) : Listener {

    @EventHandler
    fun PlayerCosmeticPostEquipEvent.onEquipBackpack() {
        if (cosmetic.slot != CosmeticSlot.BACKPACK) return
        val player = user.player ?: return
        player.toGeary().setPersisting(CosmeticComponent(player.cosmeticComponent.gesture, cosmetic.id))
        if (player.toGeary().has<BackpackStorage>()) return
        player.error("Equip a backpack (Shift + Right Click) to show the cosmetic")
        user.userBackpackManager?.despawnBackpack()
    }

    @EventHandler
    fun PlayerInteractEvent.equipBackpack() {
        if (!player.isSneaking || !rightClicked) return
        val (hand, gearyPlayer) = (hand ?: return) to player.toGeary()
        val item = player.inventory.getItem(hand)

        when {
            !player.isInventoryFull && item.type.isAir && gearyPlayer.has<BackpackStorage>() -> {
                if (player.inventory.addItem(gearyPlayer.get<BackpackStorage>()!!.backpack.clone()).isNotEmpty()) return
                gearyPlayer.remove<BackpackStorage>()
                player.unequipCosmeticBackpack()
            }

            !item.type.isAir && MaterialTags.SHULKER_BOXES.isTagged(item) && !gearyPlayer.has<BackpackStorage>() -> {
                val backpackId = Cosmetics.getCosmetic(player.cosmeticComponent.cosmeticBackpack)?.id ?: player.cosmeticComponent.cosmeticBackpack ?: feature.defaultBackpack
                gearyPlayer.setPersisting(player.cosmeticComponent.copy(cosmeticBackpack = backpackId))

                gearyPlayer.setPersisting(BackpackStorage(item.clone()))
                player.equipCosmeticBackPack(backpackId)
                item.subtract()
            }

            else -> return
        }
        setUseItemInHand(Event.Result.DENY)

    }
}
