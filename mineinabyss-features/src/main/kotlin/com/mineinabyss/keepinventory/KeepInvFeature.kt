package com.mineinabyss.keepinventory

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.arguments.booleanArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
@SerialName("keepinv")
class KeepInvFeature(
    val KeepInvInVoid: Boolean = true
) : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(KeepInvListener(this@KeepInvFeature))

        commands {
            mineinabyss {
                "keepinv"(desc = "Commands to toggle keepinventory status") {
                    val toggled by booleanArg()

                    playerAction {
                        val player = sender as Player
                        player.playerData.keepInvStatus = toggled
                        if (toggled) {
                            player.playerData.keepInvStatus = true
                            player.success("Keep Inventory enabled for ${player.name}")
                        } else {
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
