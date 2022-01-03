package com.mineinabyss.guilds

import com.mineinabyss.components.playerData
import com.mineinabyss.deeperworld.DeeperContext
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import com.mineinabyss.mineinabyss.extensions.hasGuild
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
                            guiy { player }
                        }
                    }
                }
            }
        }
    }
}
