package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class LootCrateEditingListener(val msg: LootCratesFeature.Messages) : Listener {
    @EventHandler
    fun PlayerInteractEvent.onChestInteract() {
        val chest = clickedBlock?.state as? Chest ?: return
        val pdc = chest.persistentDataContainer ?: return

        val gearyInventory = player.inventory.toGeary() ?: return
        val mainHand = gearyInventory.itemInMainHand ?: return
        val loot = mainHand.get<ContainsLoot>() ?: return

        if (!player.hasPermission(LootCratePermissions.EDIT)) {
            player.error(msg.noPermissionToEdit)
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
                chest.update()
            }
        }
        isCancelled = true
    }
}
