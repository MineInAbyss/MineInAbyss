package com.mineinabyss.features.lootcrates

import com.mineinabyss.components.lootcrates.LootTable
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import kotlinx.serialization.Serializable

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
        val tableNotFound: String = "Could not find loot table %1. Please let an administrator know!",
        val alreadyLooted: String = "You already looted this chest on %1",
        val noPermissionToEdit: String = "You don't have permission to edit loot crates",
        val noPermissionToOpen: String = "You don't have permission to open loot crates",
    )

    override fun FeatureDSL.enable() {
        plugin.listeners(*context.listeners)
    }

    override fun FeatureDSL.disable() {
        plugin.unregisterListeners(*context.listeners)
    }
}
