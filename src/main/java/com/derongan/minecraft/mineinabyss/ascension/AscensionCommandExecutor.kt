package com.derongan.minecraft.mineinabyss.ascension

import com.derongan.minecraft.mineinabyss.commands.CommandLabels
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.playerData
import com.mineinabyss.idofront.commands.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.onExecuteByPlayer
import com.mineinabyss.idofront.messaging.success

class AscensionCommandExecutor : IdofrontCommandExecutor() {
    override val commands = commands(mineInAbyss) {
        shared {
            noPermissionMessage = "The abyss laughs at your naivety"
        }

        command(CommandLabels.CURSEON) {
            onExecuteByPlayer {
                player.success("Curse enabled")
                player.playerData.isAffectedByCurse = true
            }
        }

        command(CommandLabels.CURSEOFF) {
            onExecuteByPlayer {
                player.success("Curse disabled")
                player.playerData.isAffectedByCurse = false
            }
        }
    }
}