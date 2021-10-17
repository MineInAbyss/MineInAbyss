package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.mineinabyss.configuration.MIAConfig
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
        "pvpon" {
            playerAction {
                val data = player.playerData
                if (MIAConfig.data.hubSection == WorldManager.getSectionFor(player.location)) {
                    data.pvpStatus = true
                    data.pvpUndecided = false
                    data.showPvPMessage = data.pvpUndecided
                    player.success("PVP has been enabled!")
                } else
                    sender.error("PVP can only be toggled in Orth")
            }
        }
        "pvpoff" {
            playerAction {
                val data = player.playerData
                if (MIAConfig.data.hubSection == WorldManager.getSectionFor(player.location)) {
                    data.pvpStatus = false
                    data.showPvPMessage = data.pvpUndecided
                    player.success("PVP has been disabled!")
                } else
                    sender.error("PVP can only be toggled in ${org.bukkit.ChatColor.DARK_RED}${org.bukkit.ChatColor.BOLD}Orth")
            }
        }
    }
}
