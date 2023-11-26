package com.mineinabyss.features.custom_hud

import com.mineinabyss.components.custom_hud.customHudData
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners

class CustomHudFeature(
    val backgroundLayout: String = "backgrounds",
    val customHudTemplate: String = "custom_hud"
) : Feature() {
    override val dependsOn: Set<String> get() = setOf("HappyHUD", "Packy")
    override fun FeatureDSL.enable() {
        plugin.listeners(CustomHudListener(this@CustomHudFeature))
        mainCommand {
            "custom_hud" {
                "toggle" {
                    "backgrounds" {
                        playerAction {
                            player.customHudData.showBackgrounds = !player.customHudData.showBackgrounds
                            toggleBackgroundLayouts(player, this@CustomHudFeature)
                            player.success("Backgrounds are now ${if (player.customHudData.showBackgrounds) "shown" else "hidden"}")
                        }
                    }
                }
            }
        }

        tabCompletion {
            when (args.size) {
                1 -> listOf("custom_hud").filter { it.startsWith(args[0]) }
                2 -> if (args[0] == "custom_hud") listOf("toggle").filter { it.startsWith(args[1]) } else null
                3 -> if (args[1] == "toggle") listOf("backgrounds").filter { it.startsWith(args[2]) } else null
                else -> null
            }
        }
    }
}
