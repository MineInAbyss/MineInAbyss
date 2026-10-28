package com.mineinabyss.features.mobhealth

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.requirePlugins

val MobHealthFeature = module("mob-health") {
    val config = get<AbyssFeatureConfig>().mobHealth
    require(config.enabled) { "Mob health feature is disabled" }
    requirePlugins("Emojy")

    val bars = MobHealthBars(config)
    listeners(bars)
    addCloseable { bars.clear() }
}
