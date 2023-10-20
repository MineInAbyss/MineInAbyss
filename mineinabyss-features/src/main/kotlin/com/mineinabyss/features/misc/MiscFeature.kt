package com.mineinabyss.features.misc

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class MiscFeature : Feature {
    override fun FeatureDSL.enable() {
        plugin.listeners(MiscListener())
    }
}
