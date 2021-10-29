package com.mineinabyss.hubstorage

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MIAConfig
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("hub_storage")
class HubStorageFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        commands {
            ("mineinabyss" / "mia") {
                "storage" {
                    playerAction {
                        if (MIAConfig.data.hubSection == WorldManager.getSectionFor(player.location))
                            player.openHubStorage()
                        else
                            sender.error("You are not in the hub area")
                    }
                }
            }
        }
    }
}
