package com.mineinabyss.guilds

import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.extensions.addMemberToGuild
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit

@Serializable
@SerialName("guilds")
class GuildFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(GuildListener())

        commands {
            mineinabyss {
                "add"{
                    playerAction {
                        val online = Bukkit.getOnlinePlayers()
                        online.forEach {
                            player.addMemberToGuild(it)
                            broadcast(it)
                        }
                    }
                }
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