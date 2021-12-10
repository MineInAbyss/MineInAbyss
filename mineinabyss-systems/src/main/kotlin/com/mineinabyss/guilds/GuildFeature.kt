package com.mineinabyss.guilds

import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("guilds")
class GuildFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(GuildListener())

        commands {
            mineinabyss {
//                "hasguild" {
//                    playerAction {
//                        player.playerData.hasGuild = !player.playerData.hasGuild
//                        player.playerData.hasGuild.broadcastVal("hasGuild: ")
//                    }
//                }
//                "guildowner" {
//                    playerAction {
//                        player.playerData.isGuildOwner = !player.playerData.isGuildOwner
//                        player.playerData.isGuildOwner.broadcastVal("isGuildOwner: ")
//                    }
//                }
            }
        }
    }
}