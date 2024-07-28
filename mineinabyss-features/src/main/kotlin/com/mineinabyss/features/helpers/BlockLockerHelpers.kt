package com.mineinabyss.features.helpers

import com.mineinabyss.components.blocklocker.BlockLockerLock
import com.mineinabyss.geary.papermc.datastore.decode
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.TileState
import org.bukkit.inventory.DoubleChestInventory

object BlockLockerHelpers {
    fun blockLockerTilestate(block: Block): TileState? {
        val tileState = block.state as? TileState ?: return null
        return ((tileState as? Chest)?.inventory as? DoubleChestInventory)?.let {
            return it.leftSide.location?.block?.state as? TileState ?: tileState
        } ?: tileState
    }
    fun blockLockerLock(block: Block): BlockLockerLock? {
        val tileState = blockLockerTilestate(block) ?: return null

        return ((tileState as? Chest)?.inventory as? DoubleChestInventory)?.let {
            (it.leftSide.location!!.block.state as Chest).persistentDataContainer.decode<BlockLockerLock>()
        } ?: tileState.persistentDataContainer.decode<BlockLockerLock>()
    }
}