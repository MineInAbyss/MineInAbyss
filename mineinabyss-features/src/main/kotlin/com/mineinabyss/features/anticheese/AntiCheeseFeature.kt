package com.mineinabyss.features.anticheese

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class AntiCheeseFeature : Feature {
    override fun FeatureDSL.enable() {
        if (abyss.isGSitLoaded) plugin.listeners(GSitListener())
        plugin.listeners(AntiCheeseListener())
    }
}
