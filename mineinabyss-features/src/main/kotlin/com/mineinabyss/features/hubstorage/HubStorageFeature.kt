package com.mineinabyss.features.hubstorage

import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.error
import org.bukkit.entity.Player

class HubStorageFeature : Feature() {
    override fun FeatureDSL.enable() {
        mainCommand {
            "storage"(desc = "Opens player storage") {
                playerAction {
                    val player = sender as Player
                    if (player.isInHub() || player.hasPermission(this.command.permission + ".bypass")) player.openHubStorage()
                    else player.error("You are not in the hub area.")
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("storage").filter { it.startsWith(args[0]) }
                else -> emptyList()
            }
        }
    }
}
