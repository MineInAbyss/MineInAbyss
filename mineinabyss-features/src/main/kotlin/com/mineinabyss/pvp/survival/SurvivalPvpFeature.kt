package com.mineinabyss.pvp.survival

import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.pvp.PvpDamageListener
import com.mineinabyss.pvp.PvpPrompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("survival_pvp")
class SurvivalPvpFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            SurvivalPvpListener()
        )

        commands {
            mineinabyss {
                "pvp"(desc = "Commands to toggle pvp status") {
                    playerAction {
                        if (player.location.layer?.hasPvpDefault == true) {
                            player.error("Pvp cannot be toggled in this layer.")
                            return@playerAction
                        }
                        guiy {
                            PvpPrompt(player)
                        }
                    }
                }
            }
        }
    }
}
