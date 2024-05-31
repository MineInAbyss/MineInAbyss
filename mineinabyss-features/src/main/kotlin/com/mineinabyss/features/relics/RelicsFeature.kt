package com.mineinabyss.features.relics

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class RelicsFeature : Feature() {
    override fun FeatureDSL.enable() = geary.run {
        toggleStarCompassHudAction()
        trackStarCompassHudOnPlayersSystem()

        Unit
    }
}
