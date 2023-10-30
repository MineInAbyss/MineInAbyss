package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.geary.papermc.datastore.encode
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

        val gearyInventory = player.inventory.toGeary() ?: return

        val loot = gearyInventory.itemInMainHand?.get<ContainsLoot>() ?: return

        if (!player.hasPermission(LootCratePermissions.EDIT)) {
            player.error(msg.noPermissionToEdit)
            return
        }

        if (leftClicked) {
            player.success("Removed loot table from chest")
            chest.persistentDataContainer.remove<ContainsLoot>()
        } else if (rightClicked) {
            player.success("Set loot table of chest to ${loot.table}")
            chest.persistentDataContainer.encode(loot)
            chest.update()
        }
        isCancelled = true
    }
}
