package com.mineinabyss.features.relics

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL

class RelicsFeature : Feature() {
    override fun FeatureDSL.enable() = abyss.gearyGlobal.run {
        toggleStarCompassHudAction()
        trackStarCompassHudOnPlayersSystem()

        Unit
    }
}
