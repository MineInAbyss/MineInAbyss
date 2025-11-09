package com.mineinabyss.features.curse

import com.mineinabyss.components.editPlayerData
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.success

val CurseFeature = feature("curse") {
    dependsOn {
        plugins("DeeperWorld")
    }

    onEnable {
        listeners(CurseAscensionListener(), CurseEffectsListener())

    }

    mainCommand {
        "curse" {
            description = "Commands to toggle curse"
            permission = "mineinabyss.curse"

            playerExecutes(Args.bool()) { toggled ->
                player.editPlayerData { isAffectedByCurse = toggled }
                val enabled = if (toggled) "enabled" else "disabled"
                sender.success("Curse $enabled for ${player.name}")
            }
        }
    }
}
