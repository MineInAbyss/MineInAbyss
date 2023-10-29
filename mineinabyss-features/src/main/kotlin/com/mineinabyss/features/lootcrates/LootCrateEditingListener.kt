package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.editing.LootSetting
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class LootCrateEditingListener: Listener {
    @EventHandler
    fun PlayerInteractEvent.onChestInteract() {
        val chest = clickedBlock as? Chest ?: return

        val gearyInventory = player.inventory.toGeary() ?: return

        val loot = gearyInventory.itemInMainHand?.get<LootSetting>() ?: return

        if (leftClicked) {
            chest.persistentDataContainer.remove<ContainsLoot>()
        } else if (rightClicked) {
            chest.persistentDataContainer.encode(ContainsLoot(loot.table))
        }
        isCancelled = true
    }
}
