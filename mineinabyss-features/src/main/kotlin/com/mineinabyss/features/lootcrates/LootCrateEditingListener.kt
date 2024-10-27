package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootLocation
import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class LootCrateEditingListener(val msg: LootCratesFeature.Messages) : Listener {
    @EventHandler
    fun BlockPlaceEvent.onPlaceCopiedLootChest() {
        (blockPlaced.state as? Chest)?.withGeary { chest ->
            val pdc = chest.persistentDataContainer
            val location = pdc.decode<LootLocation>() ?: return

            if (!player.hasPermission(LootCratePermissions.EDIT)) {
                pdc.remove<LootLocation>()
                pdc.remove<ContainsLoot>()
                chest.update()
                player.error(msg.noPermissionToEdit)
                isCancelled = true
                return
            }

            pdc.encode(LootLocation(chest.location))
            chest.update()
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onChestInteract() {
        (clickedBlock?.state as? Chest)?.withGeary { chest ->
            val pdc = chest.persistentDataContainer

            val gearyInventory = player.inventory.toGeary() ?: return
            val mainHand = gearyInventory.itemInMainHand ?: return
            val loot = mainHand.get<ContainsLoot>() ?: return

            if (!player.hasPermission(LootCratePermissions.EDIT)) {
                player.error(msg.noPermissionToEdit)
                isCancelled = true
                return
            }

            if (leftClicked) {
                if (pdc.has<ContainsLoot>()) {
                    player.success("Removed loot table from chest")
                    chest.persistentDataContainer.remove<ContainsLoot>()
                    chest.update()
                } else {
                    return
                }
            } else if (rightClicked) {
                if (chest.persistentDataContainer.decode<ContainsLoot>()?.table == loot.table) {
                    player.success("Previewing loot for ${loot.table}")
                    LootCratesListener.openChestWithLoot(player, loot, chest)
                } else {
                    player.success("Set loot table of chest to ${loot.table}")
                    chest.persistentDataContainer.encode(loot)
                    chest.persistentDataContainer.encode(LootLocation(chest.location))
                    chest.update()
                }
            }
            isCancelled = true
        }
    }
}
