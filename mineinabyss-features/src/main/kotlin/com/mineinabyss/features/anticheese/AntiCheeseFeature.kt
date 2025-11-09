package com.mineinabyss.features.anticheese

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.listeners

val AntiCheeseFeature : Feature = feature("anti-cheese") {
    onEnable {
        listeners(AntiCheeseListener())
    }
}
