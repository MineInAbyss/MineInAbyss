package com.mineinabyss.features.hubstorage

import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class HubStorageFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            mineinabyss {
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
}
