package com.mineinabyss.features.exp

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class ExpFeature : Feature() {
    override fun FeatureDSL.enable() {
        plugin.listeners(ExpListener())
    }
}
