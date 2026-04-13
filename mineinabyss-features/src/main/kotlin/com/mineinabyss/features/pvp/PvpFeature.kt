package com.mineinabyss.features.pvp

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player

val PvpFeature = module("pvp") {
    require(get<AbyssFeatureConfig>().pvp.enabled) { "PVP feature is disabled" }
    listeners(
        PvpDamageListener(),
        PvpListener()
    )
}.mainCommand {
    "pvp" {
        description = "Commands to toggle pvp status"
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
