package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.idofront.entities.rightClicked
import com.mineinabyss.idofront.messaging.error
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class LootCratesListener(val msg: LootCratesFeature.Messages) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun PlayerInteractEvent.onChestInteract() {
        if (!rightClicked) return
        val chest = clickedBlock?.state as? Chest ?: return
        val loot = chest.persistentDataContainer.decode<ContainsLoot>() ?: return

        if (!player.hasPermission(LootCratePermissions.OPEN)) {
            player.error(msg.noPermissionToOpen)
            return
        }

        val uuid = chest.location.toLootChestUUID()
        val lastLootDate = transaction(abyss.db) {
            LootedChests.select {
                LootedChests.playerUUID eq player.uniqueId
                LootedChests.chestId eq uuid
            }.singleOrNull()?.getOrNull(LootedChests.dateLooted)
        }
        if (lastLootDate == null) {
            transaction(abyss.db) {
                LootedChests.insert {
                    it[playerUUID] = player.uniqueId
                    it[chestId] = uuid
                    it[dateLooted] = LocalDate.now()
                }
            }
            val table = Features.lootCrates.lootTables[loot.table] ?: run {
                player.error(msg.tableNotFound.format(loot.table))
                return
            }
            val lootInventory = Bukkit.createInventory(null, 27, Component.text("Loot"))
            table.populateInventory(lootInventory)
            player.openInventory(lootInventory)
        } else {
            player.error(msg.alreadyLooted.format(lastLootDate))
        }
        isCancelled = true
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun BlockBreakEvent.removeFromDBOnChestRemove() {
        val chest = block.state as? Chest ?: return
        val loot = chest.persistentDataContainer.decode<ContainsLoot>() ?: return
        transaction(abyss.db) {
            LootedChests.deleteWhere {
                chestId eq chest.location.toLootChestUUID()
            }
        }
    }
}
