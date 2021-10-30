package com.mineinabyss.pvp.adventure

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.mineInAbyss

@ExperimentalCommandDSL
class AdventurePvpCommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
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