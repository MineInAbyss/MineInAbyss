package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.playerData
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success

@ExperimentalCommandDSL
object AscensionCommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
        shared {
            noPermissionMessage = "The abyss laughs at your naivety"
        }

        "curseon" {
            playerAction {
                player.playerData.isAffectedByCurse = true
                sender.success("Curse enabled for ${player.name}")
            }
        }

        "curseoff" {
            playerAction {
                player.playerData.isAffectedByCurse = false
                sender.error("Curse disabled for ${player.name}")
            }
        }
        "keepinvon" {
            playerAction {
                player.playerData.keepInvStatus = true
                sender.success("Keep Inventory enabled for ${player.name}")
            }
        }
        "keepinvoff" {
            playerAction {
                player.playerData.keepInvStatus = false
                sender.error("Keep Inventory disabled for ${player.name}")
            }
        }
    }
}
