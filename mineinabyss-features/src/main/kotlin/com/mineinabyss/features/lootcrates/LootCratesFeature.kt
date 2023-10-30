package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.ContainsLoot
import com.mineinabyss.components.lootcrates.LootTable
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.textcomponents.miniMsg
import kotlinx.serialization.Serializable
import org.bukkit.Material

class LootCratesFeature : FeatureWithContext<LootCratesFeature.Context>(::Context) {
    @Serializable
    class Config(val messages: Messages = Messages())
    class Context : Configurable<Config> {
        override val configManager = config("lootTables", abyss.dataPath, Config())
        val listeners = arrayOf(LootCratesListener(config.messages), LootCrateEditingListener(config.messages))
        val lootTables = geary.queryManager
            .getEntitiesMatching(family {
                hasSet<LootTable>()
                hasSet<Prefab>()
            }).associate { it.get<PrefabKey>()!!.key to it.get<LootTable>()!! }
    }

    @Serializable
    class Messages(
        val tableNotFound: String = "Could not find loot table %s. Please let an administrator know!",
        val alreadyLooted: String = "You already looted this chest on %s",
        val noPermissionToEdit: String = "You don't have permission to edit loot crates",
        val noPermissionToOpen: String = "You don't have permission to open loot crates",
    )

    override fun FeatureDSL.enable() {
        plugin.listeners(*context.listeners)

        mainCommand {
            "lootcrates" {
                "give" {
                    val lootTable by optionArg(context.lootTables.keys.toList())

                    playerAction {
                        player.inventory.addItem(
                            SerializableItemStack(
                                type = Material.STICK,
                                displayName = "<green>Loot Crate: $lootTable".miniMsg()
                            ).toItemStack().apply {
                                editMeta {
                                    it.persistentDataContainer.encode(ContainsLoot(lootTable))
                                }
                            }
                        )
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
