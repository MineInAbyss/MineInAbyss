package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootCrateConstants
import com.mineinabyss.components.lootcrates.LootTable
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.ArgsMinecraft
import com.mineinabyss.idofront.commands.brigadier.map
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.module.dsl.scopedOf

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

class LootCratesContext {
    val lootTables = with(abyss.gearyGlobal) {
        queryManager.getEntitiesMatchingAsSequence(family {
            hasSet<LootTable>()
            hasSet<PrefabKey>()
        }).map { it.toGeary() }.associate {
            it.get<PrefabKey>()!! to it.get<LootTable>()!!
        }
    }
}

val LootCratesFeature = feature("lootcrates") {
    scopedModule {
        scoped { config("lootTables", abyss.dataPath, LootCratesConfig()).getOrLoad() }
        scoped { get<LootCratesConfig>().messages }
        scopedOf(::LootCratesListener)
        scopedOf(::LootCrateEditingListener)
        scopedOf(::LootCratesContext)
    }

    onEnable {
        listeners(get<LootCratesListener>(), get<LootCrateEditingListener>())

        abyss.gearyGlobal.entity {
            set(LootTable.empty())
            set(PrefabKey.of(LootCrateConstants.CUSTOM_LOOT_TABLE))
        }
    }
    mainCommand {
        "lootcrates" {
            "give" {
                executes.asPlayer().args("item" to Args.prefabKey().oneOf { get<LootCratesContext>().lootTables.keys.toList() }) { lootTable ->
                    val (namespace, key) = lootTable
                    player.withGeary {
                        player.inventory.addItem(
                            ItemStack(Material.STICK).apply {
                                editPersistentDataContainer {
                                    it.encode(ContainsLoot(lootTable.toString()))
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
}

fun Args.prefabKey() = ArgsMinecraft.key()
    .map { PrefabKey.of(it.namespace(), it.value()) }