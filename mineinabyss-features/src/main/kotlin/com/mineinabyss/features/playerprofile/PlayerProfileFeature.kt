package com.mineinabyss.features.playerprofile

import com.mineinabyss.components.playerprofile.PlayerProfile
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.success
import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

@Serializable
data class PlayerProfileConfig(
    val enabled: Boolean = true,
    val validBackgroundIds: List<String> = listOf(),
)

val PlayerProfileFeature = feature("profiles") {
    mainCommand {
        "profile" {
            description = "Opens a players profile"

            // Self profile
            executes.asPlayer {
                guiy(player) { PlayerProfile(sender as Player, player) }
            }

            // Profile others
            executes.asPlayer().args("player" to Args.offlinePlayer()) { offlinePlayer ->
                guiy(sender as Player) { PlayerProfile(sender as Player, offlinePlayer) }
            }
        }
        "profile_background" {
            description = "Changes the background for your Player-Profile"

            executes.asPlayer().args("background" to Args.string().oneOf { get<PlayerProfileConfig>().validBackgroundIds }) { backgroundId ->
                val gearyPlayer = player.toGeary()
                val profile = gearyPlayer.get<PlayerProfile>() ?: PlayerProfile()
                gearyPlayer.setPersisting(profile.copy(background = backgroundId))
                player.success("Changed your PlayerProfile-background!")
            }
        }
    }
}
