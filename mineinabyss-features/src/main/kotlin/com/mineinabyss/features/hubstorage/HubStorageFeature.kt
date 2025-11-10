package com.mineinabyss.features.hubstorage

import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player

val HubStorageFeature = feature("hub-storage") {
    onEnable {
    }
    mainCommand {
        "storage" {
            description = "Opens player storage"
            val permission = permission
            executes.asPlayer {
                val player = sender as Player
                if (player.isInHub() || player.hasPermission("$permission.bypass")) player.openHubStorage()
                else player.error("You are not in the hub area.")
            }
        }
    }
}
