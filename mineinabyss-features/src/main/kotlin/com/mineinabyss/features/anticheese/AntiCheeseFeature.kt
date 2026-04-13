package com.mineinabyss.features.anticheese

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners

val AntiCheeseFeature = module("anticheese") {
    require(get<AbyssFeatureConfig>().antiCheese.enabled) { "AntiCheese feature is disabled" }
    listeners(AntiCheeseListener())
}
