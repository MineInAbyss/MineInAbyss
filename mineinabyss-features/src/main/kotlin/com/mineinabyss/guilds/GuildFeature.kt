package com.mineinabyss.guilds

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.mineinabyss.chatty.ChattyConfig
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.chatty.helpers.chattyConfig
import com.mineinabyss.chatty.helpers.swapChannelCommand
import com.mineinabyss.components.guilds.SpyOnGuildChat
import com.mineinabyss.deeperworld.DeeperContext
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.guilds.database.GuildRank
import com.mineinabyss.guilds.extensions.*
import com.mineinabyss.guilds.menus.GuildMainMenu
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.optionArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val guildChannel =
    ChattyConfig.Data.ChattyChannel(
        channelType = ChannelType.PRIVATE,
        proxy = false,
        discordsrv = false,
        isDefaultChannel = false,
        isStaffChannel = false,
        format = ":survival:<shift:-8>:guildchat: <chatty_nickname>: ",
        _messageColor = "gold",
        channelRadius = 0,
        channelAliases = emptyList()
    )
const val guildChannelId: String = "Guild Chat"


@Serializable
@SerialName("guilds")
class GuildFeature(
    private val guildChattyChannel: ChattyConfig.Data.ChattyChannel = guildChannel,
    val guildNameMaxLength: Int = 20,
    val guildNameBannedWords: List<String> = emptyList()
) : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        server.pluginManager.registerSuspendingEvents(GuildListener(this@GuildFeature), this)

        if (DeeperContext.isBlockLockerLoaded) {
            BlockLockerAPIv2.getPlugin().groupSystems.addSystem(GuildContainerSystem())
        }

        if (abyss.isChattyLoaded) {
            getAllGuilds().forEach {
                chattyConfig.channels.putIfAbsent(
                    "${it.guildName} $guildChannelId",
                    this@GuildFeature.guildChattyChannel
                )
            }
            listeners(ChattyGuildListener())
        }

        // Generate the guild-list
        displayGuildList()

        commands {
            mineinabyss {
                "guild"(desc = "Guild related commands") {
                    "balance"(desc = "Guild Balance related commands") {
                        "view"(desc = "View your guilds balance") {
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

                            if (!abyss.isChattyLoaded) {
                                player.error("Chatty is not loaded.")
                                return@playerAction
                            }

                            if (player.hasGuild()) {
                                val name = player.getGuildName()
                                chattyConfig.channels.putIfAbsent(
                                    "$name $guildChannelId",
                                    this@GuildFeature.guildChattyChannel
                                )
                                player.swapChannelCommand("$name $guildChannelId")
                            } else player.error("You cannot use guild chat without a guild")
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
                        "setGuildMemberRank" {
                            val player by stringArg()
                            val rank by optionArg(GuildRank.values().map { it.name })
                            action {
                                val member = Bukkit.getOfflinePlayer(player)
                                val newRank = GuildRank.valueOf(rank)

                                if (!member.hasGuild())
                                    sender.error("<b>${player}</b> does not have a guild.")
                                else if (member.getGuildRank() == newRank)
                                    sender.error("<b>${player}</b> is already a <i>$rank.")
                                else {
                                    member.getGuildName().getOwnerFromGuildName().setGuildRank(member, newRank)
                                    sender.success("Set <b>${player}</b> to <i>$rank.")
                                }

                            }
                        }
                        val guild by stringArg()
                        "addGuildMember" {
                            val player by stringArg()
                            action {
                                val member = Bukkit.getOfflinePlayer(player)
                                if (member.hasGuild() && member.getGuildName().lowercase() == guild.lowercase())
                                    sender.error("$player is already in the guild.")
                                else if (member.hasGuild())
                                    sender.error("<b>${player}</b> already has a guild.")
                                else {
                                    guild.getOwnerFromGuildName().addMemberToGuild(member)
                                    sender.success("Added <b>${player}</b> to <i>$guild.")
                                }
                            }
                        }
                        "removeGuildMember" {
                            val player by stringArg()
                            action {
                                val member = Bukkit.getOfflinePlayer(player)
                                if (!member.hasGuild())
                                    sender.error("<b>${player}</b> does not have a guild.")
                                else if (member.hasGuild() && member.getGuildName().lowercase() != guild.lowercase())
                                    sender.error("<b>${player}</b> is not in this guild.")
                                else {
                                    guild.getOwnerFromGuildName().kickPlayerFromGuild(member)
                                    sender.success("Removed <b>${player}</b> from <i>$guild.")
                                }
                            }
                        }
                        "clearJoinRequests" {
                            action {
                                if ((guild == "all")) {
                                    getAllGuildNames().forEach { it.clearGuildJoinRequests() }
                                    sender.success("Cleared all join requests for all guilds.")
                                } else {
                                    guild.clearGuildJoinRequests()
                                    sender.success("Cleared join requests for <i>$guild")
                                }
                            }
                        }
                        "clearGuildInvites" {
                            action {
                                if ((guild == "all")) {
                                    getAllGuildNames().forEach { it.clearGuildInvites() }
                                    sender.success("Cleared all guild invites for all guilds.")
                                } else {
                                    guild.clearGuildInvites()
                                    sender.success("Cleared all guild invites for <i>$guild ")
                                }
                            }
                        }
                        "guildBalance" {
                            val amount by intArg { default = 0 }
                            "set" {
                                action {
                                    guild.setGuildBalance(amount)
                                    sender.success("Set guild balance for <i>$guild</i> to $amount")
                                }
                            }
                            "add" {
                                action {
                                    if (amount <= 0) {
                                        sender.error("You can't add 0 or less coins!")
                                        return@action
                                    }
                                    guild.setGuildBalance(guild.getGuildBalance() + amount)
                                    sender.success("Added $amount coins to <i>$guild")
                                }
                            }
                            "take" {
                                action {
                                    if (amount > guild.getGuildBalance()) {
                                        sender.error("This guild doesnt have that many coins!")
                                        return@action
                                    }
                                    guild.setGuildBalance(guild.getGuildBalance() - amount)
                                    sender.success("Took $amount coins from <i>$guild")
                                }
                            }
                        }
                    }
                }
            }
            tabCompletion {
                when (args.size) {
                    1 -> listOf("guild").filter { it.startsWith(args[0]) }
                    2 -> {
                        when (args[0]) {
                            "guild" -> listOf("balance", "chat", "menu", "admin").filter { it.startsWith(args[1]) }
                            else -> null
                        }
                    }

                    3 -> {
                        when (args[1]) {
                            "balance" -> listOf("view", "deposit", "withdraw").filter { it.startsWith(args[2]) }
                            "chat" -> emptyList()
                            "menu" -> emptyList()
                            "admin" -> listOf(
                                "spy",
                                "addGuildMember",
                                "removeGuildMember",
                                "setGuildMemberRank",
                                "clearJoinRequests",
                                "clearGuildInvites",
                                "guildBalance"
                            ).filter { it.startsWith(args[2]) }

                            else -> null
                        }
                    }

                    4 -> {
                        when (args[2]) {
                            "guildBalance" -> listOf("set", "add", "take").filter { it.startsWith(args[3]) }
                            "setGuildMemberRank" ->
                                Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[3]) }

                            "clearJoinRequests", "clearGuildInvites", "addGuildMember", "removeGuildMember" ->
                                getAllGuildNames().filter { it.startsWith(args[3]) }

                            else -> null
                        }
                    }

                    5 -> {
                        when (args[2]) {
                            "guildBalance" -> getAllGuildNames().filter { it.startsWith(args[4]) }
                            "addGuildMember" -> Bukkit.getOnlinePlayers().filter { !it.hasGuild() }
                                .map { it.name }.filter { it.startsWith(args[4]) }

                            "removeGuildMember" -> Bukkit.getOnlinePlayers()
                                .filter { it.hasGuild() && it.getGuildName().lowercase() == args[3].lowercase() }
                                .map { it.name }

                            else -> null
                        }
                    }

                    6 -> {
                        when (args[2]) {
                            "guildBalance" ->
                                listOf("0", args[4].getGuildBalance().toString()).filter { it.startsWith(args[5]) }

                            else -> null
                        }
                    }

                    else -> null
                }
            }
        }
    }
}
