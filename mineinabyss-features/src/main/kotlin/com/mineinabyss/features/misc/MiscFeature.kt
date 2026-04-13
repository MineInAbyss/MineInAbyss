package com.mineinabyss.features.misc

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners

val MiscFeature = module("miscellaneous") {
    require(get<AbyssFeatureConfig>().misc.enabled) { "Misc feature is disabled" }

    listeners(MiscListener())
}
