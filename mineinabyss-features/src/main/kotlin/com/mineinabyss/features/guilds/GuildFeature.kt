package com.mineinabyss.features.guilds

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.commands.ChattyCommands
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.components.guilds.SpyOnGuildChat
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.gets
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.GuildRank
import com.mineinabyss.features.guilds.extensions.*
import com.mineinabyss.features.guilds.listeners.ChattyGuildListener
import com.mineinabyss.features.guilds.listeners.GuildContainerSystem
import com.mineinabyss.features.guilds.listeners.GuildListener
import com.mineinabyss.features.guilds.menus.GuildMainMenu
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.getOrSetPersisting
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.requirePlugins
import com.mineinabyss.idofront.features.singleConfig
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.plugin.Plugins
import kotlinx.serialization.Serializable
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.bukkit.entity.Player

const val guildChannelId: String = "Guild Chat"

interface GuildsModule {
    val config: GuildsConfig
}

@Serializable
class GuildsConfig {
    val enabled = false
    val guildChannelTemplate: ChattyChannel = ChattyChannel(
        channelType = ChannelType.CUSTOM,
        proxy = false,
        discordsrv = false,
        isDefaultChannel = false,
        isStaffChannel = false,
        messageDeletion = ChattyChannel.MessageDeletion(),
        format = ":survival::space_-4::guildchat: <chatty_nickname>: ",
        _messageColor = "gold",
        channelRadius = 0,
        channelAliases = emptyList()
    )
    val guildNameMaxLength: Int = 20
    val guildNameBannedWords: List<String> = emptyList()

    //TODO Eventually move to a per-guild config
    val canOpenGuildMemberGraves: Boolean = true
}

val GuildFeature = module("guilds") {
    requirePlugins("Chatty")

    // Dependencies
    singleConfig<GuildsConfig>("guilds.yml")

    // Enable logic
    listeners(GuildListener())

    //ProfileManager.startProfileFetching()

    if (Plugins.isEnabled("BlockLocker"))
        BlockLockerAPIv2.getPlugin().groupSystems.addSystem(GuildContainerSystem())

    if (abyss.isChattyLoaded) {
        listeners(ChattyGuildListener())
        refreshGuildChats()
    }

    // Generate the guild-list
    displayGuildList()
    single<GuildsModule> {
        object : GuildsModule {
            override val config: GuildsConfig = get()
        }
    }
}.mainCommand {
    "guild" {
        description = "Guild related commands"
        "balance" {
            description = "Guild Balance related commands"
            "view" {
                description = "View your guilds balance"
                executes.asPlayer {
                    if (!player.hasGuild()) fail("You do not have any guild.")

                    player.info("<yellow>Your guild's balance is <gold>${player.getGuildBalance()}</gold>.")
                }
            }
            "deposit" {
                description = "Deposit Orth Coins into your guild balance"
                executes.asPlayer().args(
                    "amount" to Args.integer(min = 1).default { 1 }
                ) { amount ->
                    if (amount <= 0) fail("You must deposit at least 1 coin.")
                    if (amount > player.inventory.itemInMainHand.amount) fail("You don't have that many coins.")
                    player.depositCoinsToGuild(amount)
                }
            }
            "withdraw" {
                description = "Withdraw Orth Coins from your guild balance"
                executes.asPlayer().args(
                    "amount" to Args.integer(min = 1, max = 64).default { 1 }
                ) { amount ->
                    val player = sender as Player
                    if (amount <= 0) fail("You can't withdraw 0 or less coins!")
                    player.withdrawCoinsFromGuild(amount)
                }
            }
        }
        "chat" {
            description = "Toggle guild chat"
            executes.asPlayer {
                if (!abyss.isChattyLoaded) fail("Chatty is not loaded")
                player.guildChat()
                    ?.let { ChattyCommands.swapChannel(player, it) }
                    ?: fail("You cannot use guild chat without a guild")
            }
        }
        "menu" {
            description = "Open Guild Menu"
            executes.asPlayer {
                guiy(player) { GuildMainMenu(player, true) }
            }
        }
        "admin" {
            permission = "mineinabyss.guild.admin"

            "spy" {
                executes.asPlayer {
                    val player = player.toGeary()
                    if (player.has<SpyOnGuildChat>()) player.remove<SpyOnGuildChat>()
                    else player.getOrSetPersisting<SpyOnGuildChat> { SpyOnGuildChat() }
                    sender.success("You are ${if (player.has<SpyOnGuildChat>()) "spying" else "no longer spying"} on other guild chats!")
                }
            }
            "setGuildMemberRank" {
                executes.asPlayer().args(
                    "member" to Args.offlinePlayer(),
                    //TODO enum argument helper
                    "rank" to Args.string().oneOf { GuildRank.entries.map { it.name } }.map { GuildRank.valueOf(it) }
                ) { member, newRank ->
                    if (!member.hasGuild())
                        sender.error("<b>${player}</b> does not have a guild.")
                    else if (member.getGuildRank() == newRank)
                        sender.error("<b>${player}</b> is already a <i>$newRank.")
                    else {
                        member.getGuildName()?.getOwnerFromGuildName()?.setGuildRank(member, newRank)
                        sender.success("Set <b>${player}</b> to <i>$newRank.")
                    }

                }
            }

            "addGuildMember" {
                executes.asPlayer().args(
                    "owner" to Args.offlinePlayer(),
                    "member" to Args.offlinePlayer(),
                ) { guildOwner, member ->
                    val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                    if (member.hasGuild() && member.getGuildName()?.lowercase() == guild.lowercase())
                        sender.error("${member.name} is already in the guild.")
                    else if (member.hasGuild())
                        sender.error("<b>${member.name}</b> already has a guild.")
                    else {
                        if (!guild.getOwnerFromGuildName().addMemberToGuild(member))
                            fail("Failed to add <b>${member.name}</b> to <i>$guild.")
                        sender.success("Added <b>${member.name}</b> to <i>$guild.")
                    }
                }
            }
            "removeGuildMember" {
                executes.asPlayer().args(
                    "owner" to Args.offlinePlayer(),
                    "member" to Args.offlinePlayer(),
                ) { guildOwner, member ->
                    val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                    if (!member.hasGuild())
                        sender.error("<b>${member.name}</b> does not have a guild.")
                    else if (member.hasGuild() && member.getGuildName()?.lowercase() != guild.lowercase())
                        sender.error("<b>${member.name}</b> is not in this guild.")
                    else {
                        guild.getOwnerFromGuildName().kickPlayerFromGuild(member)
                        sender.success("Removed <b>${member.name}</b> from <i>$guild.")
                    }
                }
            }
            "clearJoinRequests" {
                executes.asPlayer().args(
                    "owner" to Args.offlinePlayer(),
                ) { guildOwner ->
                    val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                    if (guild == "all") {
                        getAllGuildNames().forEach { it.clearGuildJoinRequests() }
                        sender.success("Cleared all join requests for all guilds.")
                    } else {
                        guild.clearGuildJoinRequests()
                        sender.success("Cleared join requests for <i>$guild")
                    }
                }
            }
            "clearGuildInvites" {
                executes.asPlayer().args(
                    "owner" to Args.offlinePlayer(),
                ) { guildOwner ->
                    val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                    if (guild == "all") {
                        getAllGuildNames().forEach { it.clearGuildInvites() }
                        sender.success("Cleared all guild invites for all guilds.")
                    } else {
                        guild.clearGuildInvites()
                        sender.success("Cleared all guild invites for <i>$guild ")
                    }
                }
            }
            "guildBalance" {
                "set" {
                    executes.asPlayer().args(
                        "owner" to Args.offlinePlayer(),
                        "amount" to Args.integer(min = 0)
                    ) { guildOwner, amount ->
                        val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                        guild.setGuildBalance(amount)
                        sender.success("Set guild balance for <i>$guild</i> to $amount")
                    }
                }
                "add" {
                    executes.asPlayer().args(
                        "owner" to Args.offlinePlayer(),
                        "amount" to Args.integer(min = 0)
                    ) { guildOwner, amount ->
                        val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                        guild.setGuildBalance(guild.getGuildBalance() + amount)
                        sender.success("Added $amount coins to <i>$guild")
                    }
                }
                "take" {
                    executes.asPlayer().args(
                        "owner" to Args.offlinePlayer(),
                        "amount" to Args.integer(min = 0)
                    ) { guildOwner, amount ->
                        val guild = guildOwner.getGuildName() ?: fail("This player does not have a guild.")
                        if (amount > guild.getGuildBalance()) fail("This guild doesnt have that many coins!")
                        guild.setGuildBalance(guild.getGuildBalance() - amount)
                        sender.success("Took $amount coins from <i>$guild")
                    }
                }
            }
        }
    }
}.gets<GuildsModule>()