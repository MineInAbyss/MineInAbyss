package com.mineinabyss.features.hubstorage

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player

val HubStorageFeature = module("hub-storage") {
    require(get<AbyssFeatureConfig>().hubstorage.enabled) { "Hub storage feature is disabled" }
}.mainCommand {
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
