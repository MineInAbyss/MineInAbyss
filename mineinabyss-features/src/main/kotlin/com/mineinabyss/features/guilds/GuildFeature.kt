package com.mineinabyss.features.guilds

import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.components.ChannelType
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.extensions.displayGuildList
import com.mineinabyss.features.guilds.extensions.refreshGuildChats
import com.mineinabyss.features.guilds.listeners.ChattyGuildListener
import com.mineinabyss.features.guilds.listeners.EternalFortuneGuildListener
import com.mineinabyss.features.guilds.listeners.GuildContainerSystem
import com.mineinabyss.features.guilds.listeners.GuildListener
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.Plugins
import kotlinx.serialization.Serializable
import nl.rutgerkok.blocklocker.BlockLockerAPIv2
import org.koin.core.module.dsl.scopedOf

const val guildChannelId: String = "Guild Chat"


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

val GuildFeature = feature("guilds") {
    dependsOn {
        plugins("Chatty")
    }

    scopedModule {
        scoped<GuildsConfig> { config("guilds", abyss.dataPath, GuildsConfig()).getOrLoad() }
        scopedOf(::ChattyGuildListener)
        scopedOf(::EternalFortuneGuildListener)
    }


    onEnable {
        listeners(GuildListener())
        TODO()
//        listeners(*context.listeners)

        //ProfileManager.startProfileFetching()

        if (Plugins.isEnabled("BlockLocker"))
            BlockLockerAPIv2.getPlugin().groupSystems.addSystem(GuildContainerSystem())

        if (abyss.isChattyLoaded) refreshGuildChats()

        // Generate the guild-list
        displayGuildList()

//        mainCommand {
//            "guild"(description = "Guild related commands") {
//                "balance"(description = "Guild Balance related commands") {
//                    "view"(description = "View your guilds balance") {
//                        executes.asPlayer {
//                            val player = sender as Player
//                            if (!player.hasGuild()) {
//                                player.error("You do not have any guild.")
//                                return@playerExecutes
//                            }
//
//                            player.info("<yellow>Your guild's balance is <gold>${player.getGuildBalance()}</gold>.")
//                        }
//                    }
//                    "deposit"(description = "Deposit Orth Coins into your guild balance") {
//                        val amount by intArg { default = 1 }
//                        executes.asPlayer {
//                            val player = sender as Player
//                            if (amount <= 0) {
//                                player.error("You must deposit at least 1 coin.")
//                                return@playerExecutes
//                            }
//                            if (amount > player.inventory.itemInMainHand.amount) {
//                                player.error("You don't have that many coins.")
//                                return@playerExecutes
//                            }
//                            player.depositCoinsToGuild(amount)
//                        }
//                    }
//                    "withdraw"(description = "Withdraw Orth Coins from your guild balance") {
//                        var amount by intArg { default = 1 }
//                        executes.asPlayer {
//                            val player = sender as Player
//                            if (amount <= 0) {
//                                player.error("You can't withdraw 0 or less coins!")
//                                return@playerExecutes
//                            }
//                            if (amount > 64) amount = 64
//                            player.withdrawCoinsFromGuild(amount)
//                        }
//                    }
//                }
//                "chat"(description = "Toggle guild chat") {
//                    executes.asPlayer {
//                        val player = sender as? Player ?: return@playerExecutes
//
//                        if (!abyss.isChattyLoaded) return@playerExecutes player.error("Chatty is not loaded")
//                        player.guildChat()?.let { ChattyCommands.swapChannel(player, it) }
//                            ?: return@playerExecutes player.error("You cannot use guild chat without a guild")
//                    }
//                }
//                "menu"(description = "Open Guild Menu") {
//                    executes.asPlayer {
//                        guiy(player) { GuildMainMenu(player, true) }
//                    }
//                }
//                "admin" {
//                    "spy" {
//                        executes.asPlayer {
//                            val player = (sender as Player).toGeary()
//                            if (player.has<SpyOnGuildChat>()) player.remove<SpyOnGuildChat>()
//                            else player.getOrSetPersisting<SpyOnGuildChat> { SpyOnGuildChat() }
//                            sender.success("You are ${if (player.has<SpyOnGuildChat>()) "spying" else "no longer spying"} on other guild chats!")
//                        }
//                    }
//                    "setGuildMemberRank" {
//                        val player by stringArg()
//                        val rank by optionArg(GuildRank.entries.map { it.name })
//                        action {
//                            val member = Bukkit.getOfflinePlayer(player)
//                            val newRank = GuildRank.valueOf(rank)
//
//                            if (!member.hasGuild())
//                                sender.error("<b>${player}</b> does not have a guild.")
//                            else if (member.getGuildRank() == newRank)
//                                sender.error("<b>${player}</b> is already a <i>$rank.")
//                            else {
//                                member.getGuildName()?.getOwnerFromGuildName()?.setGuildRank(member, newRank)
//                                sender.success("Set <b>${player}</b> to <i>$rank.")
//                            }
//
//                        }
//                    }
//                    val guildOwner by offlinePlayerArg()
//                    "addGuildMember" {
//                        val member by offlinePlayerArg()
//                        action {
//                            val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                            if (member.hasGuild() && member.getGuildName()?.lowercase() == guild.lowercase())
//                                sender.error("${member.name} is already in the guild.")
//                            else if (member.hasGuild())
//                                sender.error("<b>${member.name}</b> already has a guild.")
//                            else {
//                                if (!guild.getOwnerFromGuildName().addMemberToGuild(member))
//                                    return@action sender.error("Failed to add <b>${member.name}</b> to <i>$guild.")
//                                sender.success("Added <b>${member.name}</b> to <i>$guild.")
//                            }
//                        }
//                    }
//                    "removeGuildMember" {
//                        val member by offlinePlayerArg()
//                        action {
//                            val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                            if (!member.hasGuild())
//                                sender.error("<b>${member.name}</b> does not have a guild.")
//                            else if (member.hasGuild() && member.getGuildName()?.lowercase() != guild.lowercase())
//                                sender.error("<b>${member.name}</b> is not in this guild.")
//                            else {
//                                guild.getOwnerFromGuildName().kickPlayerFromGuild(member)
//                                sender.success("Removed <b>${member.name}</b> from <i>$guild.")
//                            }
//                        }
//                    }
//                    "clearJoinRequests" {
//                        action {
//                            val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                            if (guild == "all") {
//                                getAllGuildNames().forEach { it.clearGuildJoinRequests() }
//                                sender.success("Cleared all join requests for all guilds.")
//                            } else {
//                                guild.clearGuildJoinRequests()
//                                sender.success("Cleared join requests for <i>$guild")
//                            }
//                        }
//                    }
//                    "clearGuildInvites" {
//                        action {
//                            val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                            if (guild == "all") {
//                                getAllGuildNames().forEach { it.clearGuildInvites() }
//                                sender.success("Cleared all guild invites for all guilds.")
//                            } else {
//                                guild.clearGuildInvites()
//                                sender.success("Cleared all guild invites for <i>$guild ")
//                            }
//                        }
//                    }
//                    "guildBalance" {
//                        val amount by intArg { default = 0 }
//                        "set" {
//                            action {
//                                val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                                guild.setGuildBalance(amount)
//                                sender.success("Set guild balance for <i>$guild</i> to $amount")
//                            }
//                        }
//                        "add" {
//                            action {
//                                val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                                if (amount <= 0) {
//                                    sender.error("You can't add 0 or less coins!")
//                                    return@action
//                                }
//                                guild.setGuildBalance(guild.getGuildBalance() + amount)
//                                sender.success("Added $amount coins to <i>$guild")
//                            }
//                        }
//                        "take" {
//                            action {
//                                val guild = guildOwner.getGuildName() ?: return@action sender.error("This player does not have a guild.")
//                                if (amount > guild.getGuildBalance()) {
//                                    sender.error("This guild doesnt have that many coins!")
//                                    return@action
//                                }
//                                guild.setGuildBalance(guild.getGuildBalance() - amount)
//                                sender.success("Took $amount coins from <i>$guild")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        tabCompletion {
//            when (args.size) {
//                1 -> listOf("guild").filter { it.startsWith(args[0]) }
//                2 -> {
//                    when (args[0]) {
//                        "guild" -> listOf("balance", "chat", "menu", "admin").filter { it.startsWith(args[1]) }
//                        else -> null
//                    }
//                }
//
//                3 -> {
//                    when (args[1]) {
//                        "balance" -> listOf("view", "deposit", "withdraw").filter { it.startsWith(args[2]) }
//                        "chat" -> emptyList()
//                        "menu" -> emptyList()
//                        "admin" -> listOf(
//                            "spy",
//                            "addGuildMember",
//                            "removeGuildMember",
//                            "setGuildMemberRank",
//                            "clearJoinRequests",
//                            "clearGuildInvites",
//                            "guildBalance"
//                        ).filter { it.startsWith(args[2]) }
//
//                        else -> null
//                    }
//                }
//
//                4 -> {
//                    when (args[2]) {
//                        "guildBalance" -> listOf("set", "add", "take").filter { it.startsWith(args[3]) }
//                        "setGuildMemberRank" ->
//                            Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[3], true) }
//
//                        "clearJoinRequests", "clearGuildInvites", "addGuildMember", "removeGuildMember" ->
//                            getAllGuilds().map { it.guildName.getOwnerFromGuildName().name.toString() }
//                                .filter { it.startsWith(args[3], true) }
//
//                        else -> null
//                    }
//                }
//
//                5 -> {
//                    when (args[2]) {
//                        "guildBalance" -> Bukkit.getOnlinePlayers().filter { it.hasGuild() }
//                            .map { it.name }.filter { it.startsWith(args[4], true) }
//                        "addGuildMember" -> Bukkit.getOnlinePlayers().filter { !it.hasGuild() }
//                            .map { it.name }.filter { it.startsWith(args[4], true) }
//
//                        "removeGuildMember" -> Bukkit.getOfflinePlayer(args[3]).getGuildName()?.getGuildMembers()
//                            ?.map { it.player.name.toString() }?.filter { it.startsWith(args[4], true) }
//
//                        else -> null
//                    }
//                }
//
//                6 -> {
//                    when (args[2]) {
//                        "guildBalance" ->
//                            listOf("0", Bukkit.getOfflinePlayer(args[4]).getGuildBalance().toString()).filter { it.startsWith(args[5]) }
//
//                        else -> null
//                    }
//                }
//
//                else -> null
//            }
//        }
    }

    onDisable {
        ProfileManager.stopProfileFetching()
    }
}
