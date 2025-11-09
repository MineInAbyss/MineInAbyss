package com.mineinabyss.features.relics

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.features.feature

val RelicsFeature = feature("relics") {
    onEnable {
        abyss.gearyGlobal.run {
            toggleStarCompassHudAction()
            trackStarCompassHudOnPlayersSystem()
        }
    }
}
