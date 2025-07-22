package com.mineinabyss.features.guilds.extensions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.helpers.defaultChannel
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.*
import com.mineinabyss.features.guilds.guildChannelId
import com.mineinabyss.features.helpers.CoinFactory
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.inventory.toGeary
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

typealias GuildName = String

fun OfflinePlayer.createGuild(guildName: String) {
    val config = Features.guilds.config
    val newGuildName = guildName.replace("\\s".toRegex(), "") // replace space to avoid: exam ple

    if (newGuildName.length > config.guildNameMaxLength) {
        player?.error("Your guild name was longer than the maximum allowed length.")
        player?.error("Please make it shorter than ${config.guildNameMaxLength} characters.")
        return
    }

    config.guildNameBannedWords.forEach { banned ->
        val bannedWord = banned.toRegex().find(newGuildName)?.value
        if (banned.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(newGuildName)) {
            if (bannedWord?.contains("([^a-zA-Z])".toRegex()) == true)
                player?.error("Your Guild name can only contain the letters <b>a-z</b>.")
            else
                player?.error("Your Guild name contains a blocked word: <b>$bannedWord</b>.")
            player?.error("Please choose another name :)")
            return
        }
    }

    transaction(abyss.db) {

        val guild = Guilds.selectAll().where { Guilds.name.lowerCase() eq guildName.lowercase() }.firstOrNull()

        if (guild != null)
            return@transaction player?.error("There is already a guild registered with the name <i>$guildName</i>!")
        else player?.success("Your Guild has been registered with the name <i>$guildName")

        val rowID = Guilds.insert {
            it[name] = guildName
            it[balance] = 0
            it[level] = 1
            it[joinType] = GuildJoinType.ANY
        } get Guilds.id

        Players.insert {
            it[playerUUID] = uniqueId
            it[guildId] = rowID
            it[guildRank] = GuildRank.OWNER
        }
    }
}

fun Player.deleteGuild() {
    transaction(abyss.db) {
        /* Find the owners guild */
        val guildId = Players.selectAll().where { Players.playerUUID eq uniqueId }.firstOrNull()?.get(Players.guildId)
            ?: return@transaction

        if (getGuildRank() != GuildRank.OWNER) {
            this@deleteGuild.error("Only the Owner can disband the guild.")
            return@transaction
        }

        /* Message to all guild-members */
        Players.selectAll().where { (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId) }
            .forEach { row ->
                val deleteGuildMessage = "<red>The Guild you were in has been deleted by the Owner."
                val player = Bukkit.getPlayer(row[Players.playerUUID])

                player?.error(deleteGuildMessage) ?: GuildMessageQueue.insert {
                    it[content] = deleteGuildMessage
                    it[playerUUID] = row[Players.playerUUID]
                }
            }

        /* Give guildbalance to owner or one who deleted guild */
        editPlayerData {
            getGuildOwner()?.let {
                val owner = Bukkit.getOfflinePlayer(it)
                if (owner.isOnline) (owner as Player).editPlayerData { orthCoinsHeld += getGuildBalance() }
                else orthCoinsHeld += getGuildBalance()
            } ?: { orthCoinsHeld += getGuildBalance() }
        }

        val guildChatId = guildChatId()
        chatty.config.channels -= guildChatId

        // Rest will be reset when they join
        abyss.plugin.launch {
            this@deleteGuild.getGuildMembers().map { it.player }
                .filterIsInstance<Player>()
                .forEach {
                    val gearyPlayer = it.toGeary()
                    val channelData = gearyPlayer.get<ChannelData>() ?: return@forEach
                    if (channelData.channelId == guildChatId)
                        gearyPlayer.setPersisting(channelData.copy(channelId = defaultChannel().key))
                }
        }

        /* Delete join-requests & invites if the guild is deleted */
        GuildJoinQueue.deleteWhere {
            GuildJoinQueue.guildId eq guildId
        }

        /* Remove guild entry from Guilds db thus removing all members */
        Guilds.deleteWhere {
            id eq guildId
        }

        Players.deleteWhere {
            Players.guildId eq guildId
        }

        /* Message to owner */
        success("Your Guild has been deleted!")
    }
}

//TODO Make sure guild chatname is properly updated when guild name is changed
fun Player.changeStoredGuildName(newGuildName: String) {
    transaction(abyss.db) {
        val oldGuildName = getGuildName() ?: return@transaction
        val guild = Guilds.selectAll().where { Guilds.name.lowerCase() eq newGuildName.lowercase() }.firstOrNull()

        val guildId = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()[Players.guildId]



        if (guild != null) {
            error("There is already a guild registered with this name!")
            return@transaction
        }

        // Update the guildchat ID on online players, rest handled on join
        abyss.plugin.launch {
            this@changeStoredGuildName.getGuildMembers().mapNotNull { it.player.player }.forEach {
                val gearyPlayer = it.toGeary()
                val channelData = gearyPlayer.get<ChannelData>() ?: return@forEach
                if (channelData.channelId == oldGuildName.guildChatId())
                    gearyPlayer.setPersisting(channelData.copy(channelId = newGuildName.guildChatId()))
                chatty.config.channels -= oldGuildName.guildChatId()
                newGuildName.guildChat()
            }
        }

        Guilds.update({ Guilds.id eq guildId }) {
            it[name] = newGuildName
        }

        val guildName = getGuildName()
        val changedNameMessage =
            "<yellow>The Guild you are in has been renamed to <gold><i>$guildName!"
        /* Message to all guild-members */
        Players.selectAll().where { (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId) }
            .forEach { row ->

                val player = Bukkit.getPlayer(row[Players.playerUUID])
                if (player != null) {
                    player.info(changedNameMessage)
                } else {
                    GuildMessageQueue.insert {
                        it[content] = changedNameMessage
                        it[playerUUID] = row[Players.playerUUID]
                    }
                }
            }
        success("Your guild was successfully renamed to <gold><i>$guildName!")
    }
}

fun Player.changeGuildJoinType(): GuildJoinType {
    return transaction(abyss.db) {
        val guildId = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()[Players.guildId]

        val type = Guilds.selectAll().where { Guilds.id eq guildId }.single()[Guilds.joinType]

        val newType = when (type) {
            GuildJoinType.ANY -> GuildJoinType.REQUEST
            GuildJoinType.INVITE -> GuildJoinType.ANY
            GuildJoinType.REQUEST -> GuildJoinType.INVITE
        }

        Guilds.update({ Guilds.id eq guildId }) {
            it[joinType] = newType
        }

        return@transaction newType
    }
}

fun Player.getGuildMembers(): List<GuildMember> {
    return transaction(abyss.db) {
        val playerRow = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()

        val guildId = playerRow[Players.guildId]

        Players.selectAll().where { (Players.guildId eq guildId) }.map { row ->
            GuildMember(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
}

fun String.getGuildMembers(): List<GuildMember> {
    return transaction(abyss.db) {
        val guild =
            Guilds.selectAll().where { Guilds.name.lowerCase<String>() eq lowercase() }.singleOrNull()?.get(Guilds.id)
                ?: return@transaction emptyList()

        Players.selectAll().where { (Players.guildId eq guild) }.map { row ->
            GuildMember(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
}

fun getAllGuilds(): List<GuildJoin> {
    return transaction(abyss.db) {
        return@transaction Guilds.selectAll()
            .map { row -> GuildJoin(row[Guilds.name], row[Guilds.joinType], row[Guilds.level]) }
    }
}

fun getAllGuildNames(): List<String> {
    return transaction(abyss.db) {
        return@transaction Guilds.selectAll().map { row -> row[Guilds.name] }
    }
}

fun getAllGuildOwners(): List<OfflinePlayer> {
    return transaction(abyss.db) {
        return@transaction Players.selectAll().where { Players.guildRank eq GuildRank.OWNER }
            .map { Bukkit.getOfflinePlayer(it[Players.playerUUID]) }
    }
}

fun GuildName.getGuildId(): Int? {
    return transaction(abyss.db) {
        return@transaction Guilds.selectAll().where { Guilds.name.lowerCase() eq lowercase() }.firstOrNull()
            ?.get(Guilds.id)
    }
}

fun GuildName.clearGuildJoinRequests() {
    val guildId = getGuildId() ?: return
    transaction(abyss.db) {
        GuildJoinQueue.deleteWhere {
            (this.guildId eq guildId) and (joinType eq GuildJoinType.REQUEST)
        }
    }
}

fun GuildName.clearGuildInvites() {
    val guildId = getGuildId() ?: return
    transaction(abyss.db) {
        GuildJoinQueue.deleteWhere {
            (this.guildId eq guildId) and (joinType eq GuildJoinType.INVITE)
        }
    }
}

private val displayComparator = compareBy<GuildJoin> {
    it.joinType; it.guildName; it.guildName.getGuildMembers().size; it.guildLevel
}
fun displayGuildList(queryName: String? = null): List<GuildJoin> {
    val guilds = getAllGuilds()
    return when (queryName) {
        null -> guilds
        else -> guilds.filter { it.guildName.startsWith(queryName, true) }
    }.sortedWith(displayComparator).sortedByDescending { it.guildName.getGuildMembers().size; it.guildLevel }
}

fun GuildName.getOwnerFromGuildName(): OfflinePlayer {
    return transaction(abyss.db) {
        val guild = Guilds.selectAll().where { Guilds.name eq this@getOwnerFromGuildName }.first()[Guilds.id]

        val player = Players.selectAll().where { Players.guildId eq guild }.first()[Players.playerUUID]

        return@transaction Bukkit.getOfflinePlayer(player)
    }
}

fun Player.depositCoinsToGuild(amount: Int) {
    if (!hasGuild()) {
        error("You must be in a guild to withdraw coins.")
        return
    }
    if (inventory.toGeary()?.itemInMainHand?.has<OrthCoin>() != true) {
        error("You must be holding an Orth Coin to make a deposit.")
        return
    }

    inventory.itemInMainHand.subtract(amount)
    updateGuildBalance(amount)
    success("You deposited $amount Orth Coins to your guild.")
}

fun Player.withdrawCoinsFromGuild(amount: Int) {
    withGeary {
        if (!hasGuild()) {
            error("You must be in a guild to withdraw coins.")
            return
        }

        if (!isGuildOwner()) {
            error("You must be the guild owner to withdraw coins.")
            return
        }

        if (getGuildBalance() - amount < 0) {
            error("You do not have enough coins in your guild to withdraw $amount coins.")
            return
        }

        val slot = inventory.firstEmpty()
        if (slot == -1) {
            error("You do not have enough space in your inventory to withdraw the coins.")
            return
        }

        val orthCoin = CoinFactory.orthCoin
        if (orthCoin == null) {
            error("Could not create OrthCoin.")
            error("Cancelling withdrawal!")
            return
        }

        updateGuildBalance(-amount)
        inventory.addItem(orthCoin.asQuantity(amount))
        success("You withdrew $amount Orth Coins from your guild.")
    }
}

fun Player.canLevelUpGuild(): Boolean {
    val levelUpCost = getGuildName()?.getGuildLevelUpCost() ?: return false
    val balance = getGuildBalance()

    return balance >= levelUpCost
}

fun GuildName.canLevelUpGuild(): Boolean {
    val levelUpCost = getGuildLevelUpCost() ?: return false
    val balance = getGuildBalance()

    return balance >= levelUpCost
}

fun Player.levelUpGuild() {
    val guildName = getGuildName() ?: return
    val cost = guildName.getGuildLevelUpCost() ?: return
    val guildMembers = getGuildMembers().filter { it.player.uniqueId != uniqueId }.map { it.player }
    updateGuildBalance(-cost)
    transaction(abyss.db) {
        val lvl = Guilds.selectAll().where { Guilds.name.lowerCase<String>() eq guildName.lowercase() }.firstOrNull()
            ?.get(Guilds.level) ?: return@transaction 0

        Guilds.update({ Guilds.name.lowerCase() eq guildName.lowercase() }) {
            it[level] = lvl + 1
        }
    }

    val newLvl = guildName.getGuildLevel()
    val lvlUpMessage = "<gold>Your guild has leveled up to level <b>${newLvl}</b>!"

    success("You have leveled up your guild to level <b>${newLvl}</b>!")
    guildMembers.forEach { member ->
        if (member.isOnline) {
            (member as Player).info(lvlUpMessage)
        } else {
            transaction(abyss.db) {
                GuildMessageQueue.insert {
                    it[content] = lvlUpMessage
                    it[playerUUID] = member.uniqueId
                }
            }
        }
    }
    closeInventory()
}

fun GuildName.getGuildLevelUpCost(): Int? {
    return when (getGuildLevel()) {
        1 -> 25
        2 -> 50
        3 -> 100
        4 -> 200
        5 -> 400
        6 -> 700
        7 -> 1000
        8 -> 1400
        9 -> 2000
        else -> null
    }
}

private fun Player.updateGuildBalance(amount: Int) {
    transaction(abyss.db) {
        val guildId = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()[Players.guildId]

        val bal = Guilds.selectAll().where { Guilds.id eq guildId }.single()[Guilds.balance]

        Guilds.update({ Guilds.id eq guildId }) {
            it[balance] = (bal + amount)
        }
    }
}

private fun GuildName.updateGuildBalance(amount: Int) {
    transaction(abyss.db) {
        val bal = Guilds.selectAll().where { Guilds.name eq this@updateGuildBalance }.single()[Guilds.balance]

        Guilds.update({ Guilds.name eq this@updateGuildBalance }) {
            it[balance] = (bal + amount)
        }
    }
}

fun refreshGuildChats() {
    getAllGuildNames().forEach { guildName ->
        chatty.config.channels[guildName.guildChatId()] = guildName.createChattyChannel()
    }
}

/**
 * If a Guild does not have a ChattyChannel registered, this will do so
 * @return All ChattyChannels for guilds
 */
fun getAllGuildChats(): List<ChattyChannel> = getAllGuildNames().map { guildName ->
    chatty.config.channels.computeIfAbsent("$guildName $guildChannelId") {
        guildName.createChattyChannel()
    }
}

/**
 * If a Guild does not have a ChattyChannel registered, this will do so
 * @return ChattyChannel for a given guild
 */
fun GuildName.guildChat(): ChattyChannel =
    chatty.config.channels.computeIfAbsent("$this $guildChannelId") {
        createChattyChannel()
    }

/**
 * Note: If a guild does not have a registered ChattyChannel, this will register one
 * @return ChattyChannel of a players guild, or null if they are not in a guild
 */
fun Player.guildChat(): ChattyChannel? {
    val guildName = getGuildName() ?: return null
    return chatty.config.channels.computeIfAbsent("$guildName $guildChannelId") {
        guildName.createChattyChannel()
    }
}

private fun GuildName.createChattyChannel() = Features.guilds.config.guildChannelTemplate.copy(permission = "mineinabyss.guilds.chat.$this")

fun GuildName.guildChatId() = "$this $guildChannelId"
fun Player.guildChatId() = "${getGuildName()} $guildChannelId"

data class GuildJoin(val guildName: String, val joinType: GuildJoinType, val guildLevel: Int)
data class GuildMember(val rank: GuildRank, val player: OfflinePlayer)
