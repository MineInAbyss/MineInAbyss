package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootCrateConstants
import com.mineinabyss.components.lootcrates.LootTable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.GearyArgs
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.map
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Serializable
class LootCratesConfig(val messages: Messages = Messages()) {
    @Serializable
    class Messages(
        val tableNotFound: String = "Could not find loot table %s. Please let an administrator know!",
        val alreadyLooted: String = "You already looted this chest on %s",
        val noPermissionToEdit: String = "You don't have permission to edit loot crates",
        val noPermissionToOpen: String = "You don't have permission to open loot crates",
        val noPermissionToBreak: String = "You don't have permission to break loot crates, ask a member of staff if you need one removed.",
        val lootTableItemTitle: String = "<green>Loot Crate: <gold>%s<yellow>:%s",
    )

}

interface LootCrates {
    val lootTables: Map<PrefabKey, LootTable>
    fun openChestWithLoot(player: Player, loot: ContainsLoot, chest: Chest)
}

val LootCratesFeature = module("lootcrates") {
    require(get<AbyssFeatureConfig>().lootCrates.enabled) { "Lootcrates feature is disabled" }
    val config by singleConfig<LootCratesConfig>("lootTables.yml")
    single { config.messages }

    val implementation = object : LootCrates {
        override val lootTables: Map<PrefabKey, LootTable> = with(gearyPaper.worldManager.global) {
            queryManager.getEntitiesMatchingAsSequence(family {
                hasSet<LootTable>()
                hasSet<PrefabKey>()
            }).map { it.toGeary() }.associate {
                it.get<PrefabKey>()!! to it.get<LootTable>()!!
            }
        } + (LootCrateConstants.CUSTOM_LOOT_TABLE to LootTable.empty()) // Tab complete custom loot table key

        override fun openChestWithLoot(player: Player, loot: ContainsLoot, chest: Chest) {
            val lootInventory = if (loot.isCustomLoot()) {
                Bukkit.createInventory(null, 27, Component.text("Loot")).apply {
                    contents = chest.inventory.contents
                }
            } else {
                val table = lootTables[loot.table] ?: run {
                    player.error(config.messages.tableNotFound.format(loot.table))
                    return
                }
                Bukkit.createInventory(null, 27, table.itemName ?: Component.text("Loot")).apply {
                    table.populateInventory(this)
                }
            }
            player.openInventory(lootInventory)
        }
    }
    single<LootCrates> { implementation }

    listeners(new(::LootCratesListener), new(::LootCrateEditingListener))

}.mainCommand {
    "lootcrates" {
        "give" {
            executes.asPlayer().args("item" to GearyArgs.prefabKey().oneOf { get<LootCrates>().lootTables.keys.toList() }) { lootTable ->
                val (namespace, key) = lootTable
                player.withGeary {
                    player.inventory.addItem(
                        ItemStack(Material.STICK).apply {
                            editPersistentDataContainer {
                                it.encode(ContainsLoot(lootTable))
                            }
                            val itemName = get<LootCratesConfig>().messages.lootTableItemTitle.format(namespace, key).miniMsg()
                            setData(DataComponentTypes.ITEM_NAME, itemName)
                        }
                    )
                }
            }
        }
    }
}

fun Args.prefabKey() = ArgsMinecraft.key()
    .map { PrefabKey.of(it.namespace(), it.value()) }