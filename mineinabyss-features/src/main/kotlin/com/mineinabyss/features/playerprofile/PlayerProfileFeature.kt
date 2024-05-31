package com.mineinabyss.features.playerprofile

import com.mineinabyss.components.playerprofile.PlayerProfile
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

class PlayerProfileFeature(val config: Config) : Feature() {

    @Serializable
    class Config {
        val enabled = true
        val validBackgroundIds = listOf("player_profile_background")
    }


    override fun FeatureDSL.enable() {
        mainCommand {
            "profile"(desc = "Opens a players profile") {
                "background" {
                    val backgroundId by optionArg(this@PlayerProfileFeature.config.validBackgroundIds)
                    playerAction {
                        player.toGeary().setPersisting(PlayerProfile(backgroundId))
                        player.success("Changed your PlayerProfile-background!")
                    }
                }
                playerAction {
                    guiy { PlayerProfile(sender as Player, player) }
                }
            }
        }
        tabCompletion {
            val onlinePlayers = abyss.plugin.server.onlinePlayers.filter { it != sender as? Player }.map { it.name }

            when (args.size) {
                1 -> listOf("profile").filter { it.startsWith(args[0]) }
                2 -> {
                    when (args[0]) {
                        "profile" -> onlinePlayers.plus("background").filter { it.startsWith(args[1]) }
                        else -> null
                    }
                }
                3 -> this@PlayerProfileFeature.config.validBackgroundIds.filter { it.startsWith(args[2]) }

                else -> emptyList()
            }
        }
    }
}
