package com.mineinabyss.guilds

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.mineinabyss.chatty.ChattyConfig.ChattyChannel
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.swapChannelCommand
import com.mineinabyss.components.guilds.SpyOnGuildChat
import com.mineinabyss.deeperworld.DeeperContext
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.guilds.extensions.depositCoinsToGuild
import com.mineinabyss.guilds.extensions.getGuildBalance
import com.mineinabyss.guilds.extensions.hasGuild
import com.mineinabyss.guilds.extensions.withdrawCoinsFromGuild
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.commands
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.entity.Player

val guildChannel =
    ChattyChannel(
        channelType = ChannelType.PRIVATE,
        proxy = false,
        discordsrv = false,
        isDefaultChannel = false,
        isStaffChannel = false,
        format = ":survival:%chatty_shift_-4%%chatty_shift_-4%:guildchat: %chatty_player_displayname%:<gold> ",
        channelRadius = 0,
        channelAliases = emptyList()
    )

private const val guildChannelId = "guild"

@Serializable
@SerialName("guilds")
class GuildFeature(
    private val guildChattyChannel: ChattyChannel = guildChannel,
    val guildNameMaxLength: Int = 20,
    val guildNameBannedWords: List<String> = emptyList()
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        server.pluginManager.registerSuspendingEvents(GuildListener(this@GuildFeature), this)

        if (DeeperContext.isBlockLockerLoaded) {
            BlockLockerAPIv2.getPlugin().groupSystems.addSystem(GuildContainerSystem())
        }

        if (AbyssContext.isChattyLoaded)
            chattyConfig.channels.putIfAbsent(guildChannelId, this@GuildFeature.guildChattyChannel)

        commands {
            mineinabyss {
                "guild"(desc = "Guild related commands") {
                    "balance"(desc = "Guild Balance related commands") {
                        "view"(desc = "Viewe your guilds balance") {
                            playerAction {
                                val player = sender as Player
                                if (!player.hasGuild()) {
                                    player.error("You do not have any guild.")
                                    return@playerAction
                                }

                                player.info("<yellow>Your guild's balance is <gold>${player.getGuildBalance()}</gold>.")
                            }
                        }
                        "deposit"(desc = "Deposit Orth Coins into your guild balance") {
                            val amount by intArg { default = 1 }
                            playerAction {
                                val player = sender as Player
                                if (amount <= 0) {
                                    player.error("You must deposit at least 1 coin.")
                                    return@playerAction
                                }
                                if (amount > player.inventory.itemInMainHand.amount) {
                                    player.error("You don't have that many coins.")
                                    return@playerAction
                                }
                                player.depositCoinsToGuild(amount)
                            }
                        }
                        "withdraw"(desc = "Withdraw Orth Coins from your guild balance") {
                            var amount by intArg { default = 1 }
                            playerAction {
                                val player = sender as Player
                                if (amount <= 0) {
                                    player.error("You can't withdraw 0 or less coins!")
                                    return@playerAction
                                }
                                if (amount > 64) amount = 64
                                player.withdrawCoinsFromGuild(amount)
                            }
                        }
                    }
                    "chat"(desc = "Toggle guild chat") {
                        playerAction {
                            val player = sender as? Player ?: return@playerAction

                            if (player.hasGuild()) player.swapChannelCommand(guildChannelId)
                            else player.error("You cannot use guild chat without a guild")
                        }
                    }
                    "menu"(desc = "Open Guild Menu") {
                        playerAction {
                            guiy { GuildMainMenu(player, this@GuildFeature, true) }
                        }
                    }
                    "admin" {
                        "spy" {
                            playerAction {
                                val player = (sender as Player).toGeary()
                                if (player.has<SpyOnGuildChat>()) player.remove<SpyOnGuildChat>()
                                else player.getOrSetPersisting { SpyOnGuildChat() }
                                sender.success("You are ${if (player.has<SpyOnGuildChat>()) "spying" else "no longer spying"} on other guild chats!")
                            }
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
                            "guild" -> listOf("balance", "chat", "menu")
                            else -> null
                        }
                    }
                    3 -> {
                        when (args[1]) {
                            "balance" -> listOf("view", "deposit", "withdraw")
                            "chat" -> emptyList()
                            "menu" -> emptyList()
                            else -> null
                        }
                    }
                    else -> null
                }
            }
        }
    }
}
