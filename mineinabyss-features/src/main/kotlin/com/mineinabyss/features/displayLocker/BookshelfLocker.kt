package com.mineinabyss.features.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Material
import org.bukkit.block.EnchantingTable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BookshelfLocker : Listener {

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onPlace() {
        val lockState = LockDisplayItem(player.uniqueId, player.playerDataOrNull?.defaultDisplayLockState ?: false, mutableSetOf(player.uniqueId))
        (block.state as? EnchantingTable)?.withGeary { eTable ->
            eTable.persistentDataContainer.encode(lockState)
            eTable.update()
        } ?: return
        when (lockState.lockState) {
            true -> player.success("This Enchanting Table is now protected! The surrounding bookshelves are also protected!")
            false -> player.error("Use <b>/mia lock default_state</b> to protect this Enchanting Table & surrounding bookshelves.")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreakBookshelves() {
        if (block.type != Material.BOOKSHELF) return
        for (x in -2..2) for (y in -1..0) for (z in -2..2) {
            val block = block.getRelative(x, y, z)
            val lockedTable = (block.state as? EnchantingTable)?.withGeary { it.persistentDataContainer.decode<LockDisplayItem>() } ?: continue
            if (lockedTable.hasAccess(player)) continue

            isCancelled = true
            player.error("This Bookshelf is linked to a protected Enchantment Table!")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockBreakEvent.onBreak() {
        (block.state as? EnchantingTable)?.withGeary { eTable ->
            val lockedTable = eTable.persistentDataContainer.decode<LockDisplayItem>() ?: return
            if (lockedTable.hasAccess(player)) return
            isCancelled = true
            player.error("This Enchanting Table is protected!")
        }
    }
}
