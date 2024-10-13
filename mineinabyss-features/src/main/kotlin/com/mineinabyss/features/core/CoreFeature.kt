package com.mineinabyss.features.core

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners
import kotlinx.serialization.Serializable

class CoreFeature(val config: Config) : Feature() {
    @Serializable
    data class Config(
        val enabled: Boolean = false,
        val waterfallDamageMultiplier: Double = 0.5,
        val waterfallMoveMultiplier: Double = 0.15,
        val bubbleColumnDamageMultiplier: Double = 2.0,
        val bubbleColumnBreathMultiplier: Int = 2,
    )

    override fun FeatureDSL.enable() {

        plugin.listeners(CoreListener(), PreventSignEditListener())
    }
}
