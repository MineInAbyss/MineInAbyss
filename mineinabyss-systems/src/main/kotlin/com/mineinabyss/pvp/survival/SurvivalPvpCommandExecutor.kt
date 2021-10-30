package com.mineinabyss.pvp.survival

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.mineinabyss.core.mineInAbyss

@ExperimentalCommandDSL
class SurvivalPvpCommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
        "pvpon" {
            playerAction {
                if (player.location.layer?.hasPvPDefault == true) player.error("You cannot toggle PVP in this Layer")
                else {
                    player.playerData.pvpStatus = true
                    player.success("PVP has been enabled!")
                }
            }
        }
        "pvpoff" {
            playerAction {
                if (player.location.layer?.hasPvPDefault == true) player.error("You cannot toggle PVP in this Layer.")
                else {
                    player.playerData.pvpStatus = false
                    player.success("PVP has been disabled!")
                }
            }
        }
    }
}