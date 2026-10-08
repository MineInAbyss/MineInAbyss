package com.mineinabyss.features.lootcrates

import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.nexomc.nexo.api.events.furniture.NexoFurniturePlaceEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class LootCrateEditingListener(
    val msg: LootCratesConfig.Messages,
    val editMode: LootCrateEditMode,
    val overrides: CrateOverrides,
) : Listener {
    @EventHandler(priority = EventPriority.LOW)
    fun NexoFurniturePlaceEvent.onPlaceCrate() {
        if (itemInHand.containsLoot == null || player in editMode) return

        val error = if (player.hasPermission(LootCratePermissions.EDIT)) msg.editModeRequired else msg.noPermissionToEdit
        player.error(error)
        isCancelled = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun NexoFurniturePlaceEvent.coverPlacedCrate() {
        if (baseEntity.containsLoot != null) overrides.cover(mechanic.itemID)
    }

    @EventHandler
    fun InventoryCloseEvent.saveCustomLoot() {
        val editor = inventory.getHolder(false) as? CustomLootEditor ?: return
        if (!editor.crate.isValid) {
            player.inventory.addItem(*inventory.contents.filterNotNull().toTypedArray()).values
                .forEach { player.world.dropItem(player.location, it) }
            return player.error(msg.customLootNotSaved)
        }

        editor.crate.customLoot = inventory.contents.toList()
        player.success(msg.customLootSaved)
    }
}
