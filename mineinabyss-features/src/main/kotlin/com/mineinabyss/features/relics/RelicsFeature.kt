package com.mineinabyss.features.relics

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.geary.papermc.gearyWorld

val RelicsFeature = module("relics") {
    require(get<AbyssFeatureConfig>().relics.enabled) { "Relics feature is disabled" }
    gearyWorld {
        toggleStarCompassHudAction()
        trackStarCompassHudOnPlayersSystem()
    }
}
