package com.mineinabyss.features.misc

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.listeners

val MiscFeature = feature("misc") {
    override fun FeatureDSL.enable() {
        plugin.listeners(MiscListener())
    }
}
