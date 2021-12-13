package com.mineinabyss.guilds

import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
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
                "add"(desc = "Add member to players guild") {
                    val member by stringArg()
                    playerAction {
                        val invitedMember = Bukkit.getOfflinePlayer(member)
                        player.addMemberToGuild(invitedMember)
                    }
                }
            }
        }
    }
}