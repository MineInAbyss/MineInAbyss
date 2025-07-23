package com.mineinabyss.features.playerprofile

import com.mineinabyss.components.playerprofile.PlayerProfile
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.genericArg
import com.mineinabyss.idofront.commands.arguments.offlinePlayerArg
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
        val validBackgroundIds = emptyList<String>()
    }

    override fun FeatureDSL.enable() {
        mainCommand {
            "profile"(desc = "Opens a players profile") {
                playerAction {
                    guiy { PlayerProfile(sender as Player, player) }
                }
                val offlinePlayer by offlinePlayerArg { default = sender as? Player }
                action {
                    guiy { PlayerProfile(sender as Player, offlinePlayer) }
                }
            }
            "profile_background"(desc = "Changes the background for your Player-Profile") {
                val backgroundId by optionArg(this@PlayerProfileFeature.config.validBackgroundIds)
                playerAction {
                    val gearyPlayer = player.toGeary()
                    val profile = gearyPlayer.get<PlayerProfile>() ?: PlayerProfile()
                    gearyPlayer.setPersisting(profile.copy(background = backgroundId))
                    player.success("Changed your PlayerProfile-background!")
                }
            }
        }
        tabCompletion {
            when (args.size) {
                1 -> listOf("profile", "profile_background").filter { it.startsWith(args[0]) }
                2 -> {
                    when (args[0]) {
                        "profile" -> abyss.plugin.server.onlinePlayers.map { it.name }
                        "profile_background" -> config.validBackgroundIds
                        else -> null
                    }?.filter { it.startsWith(args[1], true) }
                }

                else -> emptyList()
            }
        }
    }
}
