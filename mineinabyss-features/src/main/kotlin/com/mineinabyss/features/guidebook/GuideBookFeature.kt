package com.mineinabyss.features.guidebook

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import kotlinx.serialization.Serializable

class GuideBookFeature(val config: Config) : FeatureWithContext<GuideBookFeature.Context>(::Context) {
    class Context : Configurable<GuideBookConfig> {
        override val configManager = config("guideBook", abyss.dataPath, GuideBookConfig())
    }

    @Serializable
    class Config(
        val enabled: Boolean = true,
        val frontPage: String,
    )

    override fun FeatureDSL.enable() {
        mainCommand {
            "guidebook" {
                playerAction {

                }
            }
        }
    }
}