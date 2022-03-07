package com.mineinabyss.relics

import com.mineinabyss.components.helpers.HideBossBarCompass
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.geary
import com.mineinabyss.relics.sickle.SickleListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

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
                                val player = sender as Player
                                val geary = player.toGeary()

                                if (geary.has<HideBossBarCompass>()) geary.remove<HideBossBarCompass>()
                                else geary.setPersisting(HideBossBarCompass())
                            }
                        }
                    }
                }
            }
        }
    }
}
