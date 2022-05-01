package com.mineinabyss.guilds

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.DeeperContext
import com.mineinabyss.guilds.extensions.addMemberToGuild
import com.mineinabyss.guilds.extensions.hasGuild
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Serializable
@SerialName("guilds")
class GuildFeature(
    val guildChatPrefix: String = "",
    val guildNameMaxLength: Int = 20,
    val guildNameBannedWords: List<String> = emptyList()
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(GuildListener(this@GuildFeature), GuildChatSystem(this@GuildFeature))


        if (DeeperContext.isBlockLockerLoaded) {
            BlockLockerAPIv2.getPlugin().groupSystems.addSystem(GuildContainerSystem())
        }

        commands {
            mineinabyss {
                "guild"(desc = "Guild related commands") {
                    "chat"(desc = "Toggle guild chat") {
                        playerAction {
                            val player = sender as Player
                            val data = player.playerData
                            if (!player.hasGuild()) {
                                if (data.guildChatStatus) data.guildChatStatus = false
                                player.error("You cannot use guild chat without a guild")
                                return@playerAction
                            }
                            data.guildChatStatus = !data.guildChatStatus
                            player.success("Guild chat has been toggled ${if (data.guildChatStatus) "ON" else "OFF"}!")
                        }
                    }
                    "menu"(desc = "Open Guild Menu") {
                        playerAction {
                            guiy { GuildMainMenu(player, this@GuildFeature) }
                        }
                    }
                    "test" {
                        val playerName by stringArg()
                        playerAction {
                            (sender as Player).addMemberToGuild(Bukkit.getOfflinePlayer(playerName))
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf(
                        "guild"
                    ).filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "guild" -> listOf("chat", "menu")
                            else -> null

                        }
                    }
                    else -> null
                }
            }
        }

    }
}
