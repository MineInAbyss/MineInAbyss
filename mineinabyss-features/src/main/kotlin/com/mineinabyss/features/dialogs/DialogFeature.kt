package com.mineinabyss.features.dialogs

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class DialogFeature : Feature() {

    override fun FeatureDSL.enable() {
        plugin.listeners(DialogListener())
    }
}