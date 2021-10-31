package com.mineinabyss.anticheese

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
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
@SerialName("anticheese")
@ExperimentalCommandDSL
class AntiCheeseFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(AntiCheeseListener())
        commands {
            "keepinvon" {
                playerAction {
                    player.playerData.keepInvStatus = true
                    sender.success("Keep Inventory enabled for ${player.name}")
                }
            }
            "keepinvoff" {
                playerAction {
                    player.playerData.keepInvStatus = false
                    sender.error("Keep Inventory disabled for ${player.name}")
                }
            }
        }
    }
}
