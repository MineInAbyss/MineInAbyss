package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootLocation
import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.features.lootcrates.database.LootCrate
import com.mineinabyss.features.lootcrates.database.LootCratesDataStore
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.datastore.launchWrite
import com.mineinabyss.idofront.datastore.readBlocking
import com.mineinabyss.idofront.datastore.write
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class LootCratesListener(
    val msg: LootCratesConfig.Messages,
    val lootCrates: LootCrates,
) : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    suspend fun PlayerInteractEvent.onChestInteract() {
        (clickedBlock?.state as? Chest)?.withGeary { chest ->
            val pdc = chest.persistentDataContainer
            val loot = pdc.decode<ContainsLoot>() ?: return
            val lootLocation = pdc.decode<LootLocation>() ?: return

            if (lootLocation.location != chest.location) {
                pdc.remove<ContainsLoot>()
                pdc.remove<LootLocation>()
                chest.update()
                return
            }

            if (leftClicked && !player.hasPermission(LootCratePermissions.BREAK)) {
                player.error(msg.noPermissionToBreak)
                isCancelled = true
                return
            }

            if (!rightClicked) return
            val gearyInventory = player.inventory.toGeary()
            val mainHand = gearyInventory?.itemInMainHand
            if (mainHand?.has<ContainsLoot>() == true) return

            if (!player.hasPermission(LootCratePermissions.OPEN)) {
                player.error(msg.noPermissionToOpen)
                return
            }

            val stored = player.readBlocking {
                LootCratesDataStore[player, chest.location]
            }
            val lastLootDate = stored?.dateLooted
            if (lastLootDate == null) {
                player.write {
                    LootCratesDataStore[player, chest.location] = LootCrate(
                        dateLooted = Clock.System.now(),
                        lootType = loot.table
                    )
                }
                lootCrates.openChestWithLoot(player, loot, chest)
            } else {
                val timeSinceLoot = (Clock.System.now() - lastLootDate)
                player.error(msg.alreadyLooted.format(timeSinceLoot.inWholeSeconds.seconds.toString()))
            }
            isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockBreakEvent.removeFromDBOnChestRemove() {
        (block.state as? Chest)?.withGeary { chest ->
            if (!chest.persistentDataContainer.has<ContainsLoot>()) return
            player.launchWrite {
                LootCratesDataStore.deleteAtLocation(chest.location)
            }
        }
    }
}
