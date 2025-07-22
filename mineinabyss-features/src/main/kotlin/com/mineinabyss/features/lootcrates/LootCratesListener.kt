package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootLocation
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.lootcrates.constants.LootCratePermissions
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.features.lootcrates.database.LootedChests.locationEq
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.has
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.entities.leftClicked
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDate

class LootCratesListener(val msg: LootCratesFeature.Messages) : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerInteractEvent.onChestInteract() {
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

            val lastLootDate = transaction(abyss.db) {
                LootedChests.selectAll()
                    .where { (LootedChests.playerUUID eq player.uniqueId) and locationEq(chest.location) }.singleOrNull()
                    ?.getOrNull(LootedChests.dateLooted)
            }
            if (lastLootDate == null) {
                transaction(abyss.db) {
                    LootedChests.insert {
                        it[playerUUID] = player.uniqueId
                        it[x] = chest.location.blockX
                        it[y] = chest.location.blockY
                        it[z] = chest.location.blockZ
                        it[world] = chest.location.world.name
                        it[dateLooted] = LocalDate.now()
                        it[lootType] = loot.table
                    }
                }
                openChestWithLoot(player, loot, chest)
            } else {
                player.error(msg.alreadyLooted.format(lastLootDate))
            }
            isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockBreakEvent.removeFromDBOnChestRemove() {
        (block.state as? Chest)?.withGeary { chest ->
            if (!chest.persistentDataContainer.has<ContainsLoot>()) return
            transaction(abyss.db) {
                LootedChests.deleteWhere {
                    locationEq(chest.location)
                }
            }
        }
    }

    companion object {
        fun openChestWithLoot(player: Player, loot: ContainsLoot, chest: Chest) {
            val lootInventory = if (loot.isCustomLoot()) {
                Bukkit.createInventory(null, 27, Component.text("Loot")).apply {
                    contents = chest.inventory.contents
                }
            } else {
                val table = Features.lootCrates.lootTables[loot.table] ?: run {
                    player.error(Features.lootCrates.config.messages.tableNotFound.format(loot.table))
                    return
                }
                Bukkit.createInventory(null, 27, table.itemName ?: Component.text("Loot")).apply {
                    table.populateInventory(this)
                }
            }
            player.openInventory(lootInventory)
        }
    }
}
