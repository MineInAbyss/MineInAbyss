package com.mineinabyss.features.blocklocker

import com.mineinabyss.components.blocklocker.BlockLockerLock
import com.mineinabyss.features.helpers.BlockLockerHelpers
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.datastore.remove
import org.bukkit.block.Chest
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.DoubleChestInventory

class BlockLockerListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockPlaceEvent.onPlaceChest() {
        val placedData = (blockPlaced.blockData as? org.bukkit.block.data.type.Chest)?.takeUnless { it.type == org.bukkit.block.data.type.Chest.Type.SINGLE } ?: return
        val tileState = BlockLockerHelpers.blockLockerTilestate(blockPlaced) as? Chest ?: return
        val doubleInv = tileState.blockInventory as? DoubleChestInventory ?: return

        val rightPdc = (doubleInv.rightSide.location!!.block.state as Chest).persistentDataContainer
        if (placedData.type == org.bukkit.block.data.type.Chest.Type.LEFT) {
            rightPdc.decode<BlockLockerLock>()?.let { tileState.persistentDataContainer.encode(it) }
            rightPdc.remove<BlockLockerLock>()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun BlockBreakEvent.onBreakChest() {
        val placedData = (block.blockData as? org.bukkit.block.data.type.Chest)?.takeUnless { it.type == org.bukkit.block.data.type.Chest.Type.SINGLE } ?: return
        val tileState = BlockLockerHelpers.blockLockerTilestate(block) as? Chest ?: return
        val doubleInv = tileState.blockInventory as? DoubleChestInventory ?: return

        if (placedData.type == org.bukkit.block.data.type.Chest.Type.LEFT) {
            val rightPdc = (doubleInv.rightSide.location!!.block.state as Chest).persistentDataContainer
            tileState.persistentDataContainer.decode<BlockLockerLock>()?.let { rightPdc.encode(it) }
        }
    }
}