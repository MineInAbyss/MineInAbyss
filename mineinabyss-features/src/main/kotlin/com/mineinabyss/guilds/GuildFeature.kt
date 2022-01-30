package com.mineinabyss.guilds

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.DeeperContext
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.extensions.hasGuild
import com.mineinabyss.npc.orthbanking.ui.BankMenu
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.rutgerkok.blocklocker.BlockLockerAPIv2

@Serializable
@SerialName("guilds")
class GuildFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(GuildListener(), GuildChatSystem())


        if (DeeperContext.isBlockLockerLoaded) {
            BlockLockerAPIv2.getPlugin().groupSystems.addSystem(GuildContainerSystem())
        }

        commands {
            mineinabyss {
                "guild"(desc = "Guild related commands") {
                    "chat"(desc = "Toggle guild chat") {
                        playerAction {
                            if (!player.hasGuild()) {
                                player.error("You cannot use guild chat without a guild")
                                return@playerAction
                            }
                            player.playerData.guildChatStatus = !player.playerData.guildChatStatus
                            player.success("Guild chat has been toggled ${if (player.playerData.guildChatStatus) "ON" else "OFF"}!")
                        }
                    }
                    "menu"(desc = "Open Guild Menu") {
                        playerAction {
                            guiy { GuildMainMenu(player) }
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