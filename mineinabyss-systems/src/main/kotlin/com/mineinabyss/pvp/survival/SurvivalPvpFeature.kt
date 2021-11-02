package com.mineinabyss.pvp.survival

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.pvp.PvpDamageListener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("survival_pvp")
@ExperimentalCommandDSL
class SurvivalPvpFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            SurvivalPvpListener()
        )

        commands {
            "pvp" {
                "on" {
                    playerAction {
                        val data = player.playerData
                        if (player.location.layer?.hasPvPDefault == false) {
                            data.pvpStatus = true
                            data.pvpUndecided = false
                            data.showPvPMessage = true
                            player.success("PvP has been enabled.")
                        } else sender.error("PvP can not be toggled in this layer.")
                    }
                }
                "off" {
                    playerAction {
                        val data = player.playerData
                        if (player.location.layer?.hasPvPDefault == false) {
                            data.pvpStatus = false
                            data.pvpUndecided = false
                            data.showPvPMessage = true
                            player.success("PvP has been disabled.")
                        } else sender.error("PvP can not be toggled in this layer.")
                    }
                }
            }
        }
    }
}