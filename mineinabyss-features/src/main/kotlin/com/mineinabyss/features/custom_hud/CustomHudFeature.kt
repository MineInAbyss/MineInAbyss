package com.mineinabyss.features.custom_hud

import com.mineinabyss.components.custom_hud.customHudData
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.koin.core.module.dsl.scopedOf

@Serializable
data class CustomHudConfig(
    val backgroundLayout: String = "backgrounds",
    val customHudTemplate: String = "custom_hud",
)

val CustomHudFeature = feature("custom-hud") {
    dependsOn {
        plugins("MythicHUD", "Packy")
    }

    scopedModule {
        scopedOf(::CustomHudConfig) //TODO make configurable
        scopedOf(::CustomHuds)
        scopedOf(::CustomHudListener)
    }

    onEnable {
        listeners(get<CustomHudListener>())
    }

    mainCommand {
        "custom_hud" {
            "toggle" {
                "backgrounds" {
                    executes.asPlayer {
                        player.customHudData.showBackgrounds = !player.customHudData.showBackgrounds
                        get<CustomHuds>().toggleBackgroundLayouts(player)
                        player.success("Backgrounds are now ${if (player.customHudData.showBackgrounds) "shown" else "hidden"}")
                    }
                }
            }
        }
    }
}
