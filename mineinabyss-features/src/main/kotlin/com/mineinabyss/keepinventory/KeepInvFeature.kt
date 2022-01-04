package com.mineinabyss.keepinventory

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("keepinv")
class KeepInvFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(KeepInvListener())

        commands {
            mineinabyss {
                "keepinv"(desc = "Commands to toggle keepinventory status") {
                    "on" {
                        playerAction {
                            player.playerData.keepInvStatus = true
                            sender.success("Keep Inventory enabled for ${player.name}")
                        }
                    }
                    "off" {
                        playerAction {
                            player.playerData.keepInvStatus = false
                            sender.error("Keep Inventory disabled for ${player.name}")
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "keepinv"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "keepinv" -> listOf("on", "off")
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }
    }
}
