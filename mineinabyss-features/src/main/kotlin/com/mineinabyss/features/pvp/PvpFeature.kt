package com.mineinabyss.features.pvp

import com.mineinabyss.features.helpers.layer
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player

val PvpFeature = feature("pvp") {
    onEnable {
        listeners(
            PvpDamageListener(),
            PvpListener()
        )
    }

    mainCommand {
        "pvp" {
            //TODO description "Commands to toggle pvp status"
            executes.asPlayer {
                val player = sender as Player
                if (player.location.layer?.hasPvpDefault == true) {
                    player.error("Pvp cannot be toggled in this layer.")
                    return@asPlayer
                }
                guiy(player) { PvpPrompt(player) }
            }
        }
    }
}
