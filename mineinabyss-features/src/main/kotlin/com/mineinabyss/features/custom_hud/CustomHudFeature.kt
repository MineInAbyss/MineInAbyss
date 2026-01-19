package com.mineinabyss.features.custom_hud

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class CustomHudFeature(
    val backgroundLayout: String = "backgrounds",
    val customHudTemplate: String = "custom_hud"
) : Feature() {
    override val dependsOn: Set<String> get() = setOf("MythicHUD", "Packy")
    override fun FeatureDSL.enable() {
        plugin.listeners(CustomHudListener(this@CustomHudFeature))
        mainCommand {
            "custom_hud" {
            }
        }

        tabCompletion {
            when (args.size) {
                1 -> listOf("custom_hud").filter { it.startsWith(args[0]) }
                else -> null
            }
        }
    }
}
