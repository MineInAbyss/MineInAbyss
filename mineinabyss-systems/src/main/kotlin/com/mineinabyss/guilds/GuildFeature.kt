package com.mineinabyss.guilds

import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.data.GuildJoinQueue
import com.mineinabyss.mineinabyss.data.Guilds
import com.mineinabyss.mineinabyss.data.Players
import com.mineinabyss.mineinabyss.extensions.addMemberToGuild
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

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
                "clearDB" {
                    playerAction {
                        transaction {
                            GuildJoinQueue.deleteAll()
                            Players.deleteAll()
                            Guilds.deleteAll()
                            player.success("Cleared Guild Database")
                        }
                    }
                }
            }
        }
    }
}