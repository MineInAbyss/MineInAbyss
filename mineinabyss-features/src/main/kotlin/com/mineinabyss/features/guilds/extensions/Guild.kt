package com.mineinabyss.features.guilds.extensions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.ChattyChannel
import com.mineinabyss.chatty.chatty
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.helpers.defaultChannel
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.npc.orthbanking.OrthCoin
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildMessagesTable
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.data.tables.GuildsTable
import com.mineinabyss.features.guilds.data.tables.GuildMembersTable
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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

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

        val guild = GuildsTable.selectAll().where { GuildsTable.name.lowerCase() eq guildName.lowercase() }.firstOrNull()

        if (guild != null)
            return@transaction player?.error("There is already a guild registered with the name <i>$guildName</i>!")
        else player?.success("Your Guild has been registered with the name <i>$guildName")

        val rowID = GuildsTable.insert {
            it[name] = guildName
            it[balance] = 0
            it[level] = 1
            it[joinType] = GuildJoinType.ANY
        } get GuildsTable.id

        GuildMembersTable.insert {
            it[id] = uniqueId
            it[this.guild] = rowID
            it[guildRank] = GuildRank.OWNER
        }
    }
}

fun Player.deleteGuild() {
    transaction(abyss.db) {
        /* Find the owners guild */
        val guildId = GuildMembersTable.selectAll().where { GuildMembersTable.id eq uniqueId }.firstOrNull()?.get(GuildMembersTable.guild)?.value
            ?: return@transaction

        if (getGuildRank() != GuildRank.OWNER) {
            this@deleteGuild.error("Only the Owner can disband the guild.")
            return@transaction
        }

        /* Message to all guild-members */
        GuildMembersTable.selectAll().where { (GuildMembersTable.guild eq guildId) and (GuildMembersTable.id neq uniqueId) }
            .forEach { row ->
                val deleteGuildMessage = "<red>The Guild you were in has been deleted by the Owner."
                val player = Bukkit.getPlayer(row[GuildMembersTable.id].value)

                player?.error(deleteGuildMessage) ?: GuildMessagesTable.insert {
                    it[content] = deleteGuildMessage
                    it[playerUUID] = row[GuildMembersTable.id].value
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
        GuildJoinRequestsTable.deleteWhere {
            GuildJoinRequestsTable.guildId eq guildId
        }

        /* Remove guild entry from Guilds db thus removing all members */
        GuildsTable.deleteWhere {
            id eq guildId
        }

        GuildMembersTable.deleteWhere {
            GuildMembersTable.guild eq guildId
        }

        /* Message to owner */
        success("Your Guild has been deleted!")
    }
}

fun Player.getGuildMembers(): List<GuildMember> {
    return transaction(abyss.db) {
        val playerRow = GuildMembersTable.selectAll().where { GuildMembersTable.id eq uniqueId }.single()

        val guildId = playerRow[GuildMembersTable.guild]

        GuildMembersTable.selectAll().where { (GuildMembersTable.guild eq guildId) }.map { row ->
            GuildMember(row[GuildMembersTable.guildRank], Bukkit.getOfflinePlayer(row[GuildMembersTable.id].value))
        }
    }
}

fun String.getGuildMembers(): List<GuildMember> {
    return transaction(abyss.db) {
        val guild =
            GuildsTable.selectAll().where { GuildsTable.name.lowerCase<String>() eq lowercase() }.singleOrNull()?.get(GuildsTable.id)
                ?: return@transaction emptyList()

        GuildMembersTable.selectAll().where { (GuildMembersTable.guild eq guild) }.map { row ->
            GuildMember(row[GuildMembersTable.guildRank], Bukkit.getOfflinePlayer(row[GuildMembersTable.id].value))
        }
    }
}

fun getAllGuilds(): List<GuildJoin> {
    return transaction(abyss.db) {
        return@transaction GuildsTable.selectAll()
            .map { row -> GuildJoin(row[GuildsTable.name], row[GuildsTable.joinType], row[GuildsTable.level]) }
    }
}

fun getAllGuildNames(): List<String> {
    return transaction(abyss.db) {
        return@transaction GuildsTable.selectAll().map { row -> row[GuildsTable.name] }
    }
}

fun GuildName.getGuildId(): Int? {
    return transaction(abyss.db) {
        return@transaction GuildsTable.selectAll().where { GuildsTable.name.lowerCase<String>() eq lowercase() }.firstOrNull()
            ?.get(GuildsTable.id)
            ?.value
    }
}

fun GuildName.clearGuildJoinRequests() {
    val guildId = getGuildId() ?: return
    transaction(abyss.db) {
        GuildJoinRequestsTable.deleteWhere {
            (this.guildId eq guildId) and (joinType eq GuildJoinType.REQUEST)
        }
    }
}

fun GuildName.clearGuildInvites() {
    val guildId = getGuildId() ?: return
    transaction(abyss.db) {
        GuildJoinRequestsTable.deleteWhere {
            (this.guildId eq guildId) and (joinType eq GuildJoinType.INVITE)
        }
    }
}

fun displayGuildList(queryName: String? = null): List<GuildJoin> {
    val guilds = getAllGuilds()
    val comparator = compareBy<GuildJoin> {
        it.joinType; it.guildName; it.guildName.getGuildMembers().size; it.guildLevel
    }
    return when (queryName) {
        null -> guilds
        else -> guilds.filter { it.guildName.startsWith(queryName, true) }
    }.sortedWith(comparator).sortedByDescending { it.guildName.getGuildMembers().size; it.guildLevel }
}

fun GuildName.getOwnerFromGuildName(): OfflinePlayer {
    return transaction(abyss.db) {
        val guild = GuildsTable.selectAll().where { GuildsTable.name eq this@getOwnerFromGuildName }.first()[GuildsTable.id]

        val player = GuildMembersTable.selectAll().where { GuildMembersTable.guild eq guild }.first()[GuildMembersTable.id].value

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
        val lvl = GuildsTable.selectAll().where { GuildsTable.name.lowerCase<String>() eq guildName.lowercase() }.firstOrNull()
            ?.get(GuildsTable.level) ?: return@transaction 0

        GuildsTable.update({ GuildsTable.name.lowerCase() eq guildName.lowercase() }) {
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
                GuildMessagesTable.insert {
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
        val guildId = GuildMembersTable.selectAll().where { GuildMembersTable.id eq uniqueId }.single()[GuildMembersTable.guild]

        val bal = GuildsTable.selectAll().where { GuildsTable.id eq guildId }.single()[GuildsTable.balance]

        GuildsTable.update({ GuildsTable.id eq guildId }) {
            it[balance] = (bal + amount)
        }
    }
}

private fun GuildName.updateGuildBalance(amount: Int) {
    transaction(abyss.db) {
        val bal = GuildsTable.selectAll().where { GuildsTable.name eq this@updateGuildBalance }.single()[GuildsTable.balance]

        GuildsTable.update({ GuildsTable.name eq this@updateGuildBalance }) {
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
