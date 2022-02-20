package com.mineinabyss.relics

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.relics.sickle.SickleListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("relics")
class RelicsFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {

        geary {
            systems(
                ShowDepthListener(),
            )
        }
        registerEvents(SickleListener())

        commands {
            mineinabyss {
                "relics"(desc = "Commands for relic-related stuff") {
                    "star_compass"(desc = "Commands related to the Star Compass") {
                        "toggle" {
                            playerAction {
                                player.playerData.starCompassToggle = !player.playerData.starCompassToggle
                            }
                        }
                    }
                }
            }
        }
    }
}
