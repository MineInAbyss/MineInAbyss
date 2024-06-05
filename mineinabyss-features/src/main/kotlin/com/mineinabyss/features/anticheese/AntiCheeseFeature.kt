package com.mineinabyss.features.anticheese

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class AntiCheeseFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.listeners(AntiCheeseListener())
    }
}
