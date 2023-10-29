package com.mineinabyss.features.lootcrates

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.unregisterListeners
import kotlinx.serialization.Serializable

class LootCratesFeature : FeatureWithContext<LootCratesFeature.Context>(::Context) {
    @Serializable
    class Config()
    class Context : Configurable<Config> {
        override val configManager = config("lootTables", abyss.dataPath, Config())
        val listeners = arrayOf(LootCratesListener())
    }

    override fun FeatureDSL.enable() {
        plugin.listeners(*context.listeners)
    }

    override fun FeatureDSL.disable() {
        plugin.unregisterListeners(*context.listeners)
    }
}
