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
import com.mineinabyss.mineinabyss.isInHub
import com.mineinabyss.pvp.PvpDamageListener
import com.mineinabyss.pvp.adventure.PromptAdventurePvpSelect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("adventure_pvp")
@ExperimentalCommandDSL
class AdventurePvpFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(
            PvpDamageListener(),
            PromptAdventurePvpSelect()
        )

        commands {
            "pvp" {
                "on" {
                    playerAction {
                        val data = player.playerData
                        if (player.isInHub()) {
                            data.pvpStatus = true
                            data.pvpUndecided = false
                            player.success("PvP has been enabled.")
                        } else sender.error("PvP can only be toggled in Orth.")
                    }
                }
                "off" {
                    playerAction {
                        val data = player.playerData
                        if (player.isInHub()) {
                            data.pvpStatus = false
                            data.pvpUndecided = false
                            player.success("PvP has been disabled.")
                        } else sender.error("PvP can only be toggled in Orth.")
                    }
                }
                "message" {
                    playerAction {
                        val data = player.playerData
                        data.showPvPMessage = !data.showPvPMessage
                        data.pvpUndecided = false
                        player.success("PvP message has been ${if (data.showPvPMessage) "enabled" else "disabled"}.")

                    }
                }
            }
        }
    }
}