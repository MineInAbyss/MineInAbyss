package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootPool
import com.mineinabyss.components.lootcrates.LootSelector
import com.mineinabyss.components.lootcrates.Roll
import com.mineinabyss.features.abyss
import com.mineinabyss.features.lootcrates.database.LootedChests
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.serialization.SerializableItemStack
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class LootCratesListener : Listener {
    @EventHandler
    fun PlayerInteractEvent.onChestInteract() {
        val chest = clickedBlock as? Chest ?: return
        val loot = chest.persistentDataContainer.decode<ContainsLoot>() ?: return

        val uuid = chest.location.toLootChestUUID()
        val lastLootDate = transaction(abyss.db) {
            LootedChests.select {
                LootedChests.playerUUID eq player.uniqueId
                LootedChests.chestId eq uuid
            }.singleOrNull()?.getOrNull(LootedChests.dateLooted)
        }
        if (lastLootDate == null) {
            LootedChests.insert {
                it[playerUUID] = player.uniqueId
                it[chestId] = uuid
                it[dateLooted] = LocalDate.now()
            }
            // TODO get table by name
            loot.table
            val table = com.mineinabyss.components.lootcrates.LootTable(
                Roll.Constant(5),
                listOf(
                    LootPool(
                        listOf(
                            LootSelector.IdofrontItem(
                                SerializableItemStack(type = Material.STONE)
                            )
                        )
                    )
                )
            )
            val lootInventory = Bukkit.createInventory(player, 27, Component.text("Loot"))
            table.populateInventory(lootInventory)
            player.openInventory(lootInventory)
            player.success("Looted chest with $loot!")
        } else {
            player.error("You already looted this chest on $lastLootDate")
        }
    }
}
