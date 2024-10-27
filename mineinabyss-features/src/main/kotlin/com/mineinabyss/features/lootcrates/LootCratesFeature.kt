package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootCrateConstants
import com.mineinabyss.components.lootcrates.LootTable
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.cId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getComponentInfo
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.get
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.EntityTracking
import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOf
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class LootCratesFeature : FeatureWithContext<LootCratesFeature.Context>(::Context) {
    @Serializable
    class Config(val messages: Messages = Messages())
    class Context : Configurable<Config> {
        override val configManager = config("lootTables", abyss.dataPath, Config())
        val listeners = arrayOf(LootCratesListener(config.messages), LootCrateEditingListener(config.messages))
        val lootTables by lazy {
            with(abyss.gearyGlobal) {
                queryManager.getEntitiesMatchingAsSequence(family {
                    hasSet(componentId<LootTable>())
                    hasSet(componentId<PrefabKey>())
                }).map { it.toGeary() }.associate {
                    it.get<PrefabKey>()!!.toString() to it.get<LootTable>()!!
                }
            }
        }
    }

    @Serializable
    class Messages(
        val tableNotFound: String = "Could not find loot table %s. Please let an administrator know!",
        val alreadyLooted: String = "You already looted this chest on %s",
        val noPermissionToEdit: String = "You don't have permission to edit loot crates",
        val noPermissionToOpen: String = "You don't have permission to open loot crates",
        val noPermissionToBreak: String = "You don't have permission to break loot crates, ask a member of staff if you need one removed.",
        val lootTableItemTitle: String = "<green>Loot Crate: <gold>%s<yellow>:%s",
    )

    override fun FeatureDSL.enable() {
        plugin.listeners(*context.listeners)

        abyss.gearyGlobal.entity {
            set(LootTable.empty())
            set(PrefabKey.of(LootCrateConstants.CUSTOM_LOOT_TABLE))
        }

        mainCommand {
            "lootcrates" {
                "give" {
                    val lootTable by optionArg(context.lootTables.keys.toList())

                    playerAction {
                        val (namespace, key) = PrefabKey.of(lootTable)
                        player.withGeary {
                            player.inventory.addItem(
                                ItemStack(Material.STICK).editItemMeta {
                                    itemName(
                                        context.config.messages.lootTableItemTitle.format(namespace, key).miniMsg()
                                    )
                                    persistentDataContainer.encode(ContainsLoot(lootTable))
                                }
                            )
                        }
                    }
                }
            }
        }
        tabCompletion {
            when (args.getOrNull(0)) {
                "lootcrates" -> {
                    when (args.getOrNull(1)) {
                        "give" ->
                            if (args.size == 3) context.lootTables.keys.toList()
                                .filter { it.startsWith(args[2], ignoreCase = true) }
                            else null

                        else -> if (args.size == 2) listOf("give") else null
                    }
                }

                else ->
                    if (args.size == 1) listOf("lootcrates").filter { it.startsWith(args[0], ignoreCase = true) }
                    else null
            }
        }
    }

    override fun FeatureDSL.disable() {
        plugin.unregisterListeners(*context.listeners)
    }
}
