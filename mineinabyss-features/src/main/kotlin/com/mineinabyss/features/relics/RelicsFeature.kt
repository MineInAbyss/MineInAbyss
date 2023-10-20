package com.mineinabyss.features.relics

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL

class RelicsFeature : Feature {
    override fun FeatureDSL.enable() {
        geary.pipeline.addSystems(
            ToggleStarCompassHudSystem(),
            ToggleStarCompassHud(),
        )
    }
}
