package com.mineinabyss.guilds.extensions

import com.mineinabyss.guilds.GuildFeature
import com.mineinabyss.guilds.database.*
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.guilds.extensions.getGuildMemberCount
import com.mineinabyss.guilds.extensions.getGuildName
import com.mineinabyss.guilds.extensions.getGuildRank
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Player.createGuild(guildName: String, feature: GuildFeature) {
    val newGuildName = guildName.replace("\\s".toRegex(), "") // replace space to avoid: exam ple

    if (newGuildName.length > feature.guildNameMaxLength) {
        player?.error("Your guild name was longer than the maximum allowed length.")
        player?.error("Please make it shorter than ${feature.guildNameMaxLength} characters.")
        return
    }

    feature.guildNameBannedWords.forEach { banned ->
        val bannedWord = banned.toRegex().find(newGuildName)?.value
        if (banned.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(newGuildName)) {
            if (bannedWord?.contains("([^a-zA-Z])".toRegex()) == true)
                player?.error("Your Guild name can only contain the letters ${ChatColor.BOLD}a-z.")
            else
                player?.error("Your Guild name contains a blocked word: ${ChatColor.BOLD}${bannedWord}.")
            player?.error("Please choose another name :)")
            return
        }
    }

    transaction(AbyssContext.db) {

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.firstOrNull()

        if (guild != null){
            player?.error("There is already a guild registered with the name $guildName!")
            return@transaction
        }
        else player?.success("Your Guild has been registered with the name ${ChatColor.ITALIC}$guildName")

        val rowID = Guilds.insert {
            it[name] = guildName
            it[balance] = 0
            it[level] = 1
            it[joinType] = GuildJoinType.Any
        } get Guilds.id

        Players.insert {
            it[playerUUID] = uniqueId
            it[guildId] = rowID
            it[guildRank] = GuildRanks.Owner
        }
    }
}

fun OfflinePlayer.deleteGuild() {
    //TODO Handle Guild Tokens
    transaction(AbyssContext.db) {
        /* Find the owners guild */
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()!![Players.guildId]

        if (player?.getGuildRank() != GuildRanks.Owner) {
            player?.error("Only the Owner can disband the guild.")
            return@transaction
        }

        /* Message to all guild-members */
        Players.select {
            (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId)
        }.forEach { row ->
            val deleteGuildMessage = "${ChatColor.RED}The Guild you were in has been deleted by the Owner."
            val player = Bukkit.getPlayer(row[Players.playerUUID])
            if (player != null) {
                player.error(deleteGuildMessage)
            } else {
                MessageQueue.insert {
                    it[content] = deleteGuildMessage
                    it[playerUUID] = row[Players.playerUUID]
                }
            }
        }

        /* Delete join-requests & invites if the guild is deleted */
        GuildJoinQueue.deleteWhere {
            GuildJoinQueue.guildId eq guildId
        }

        /* Remove guild entry from Guilds db thus removing all members */
        Guilds.deleteWhere {
            Guilds.id eq guildId
        }

        Players.deleteWhere {
            Players.guildId eq guildId
        }


        /* Remove guild entry from Guilds db thus removing all members */

        /* Message to owner */
        this@deleteGuild.player?.success("Your Guild has been deleted!")
    }
}

fun Player.changeStoredGuildName(newGuildName: String) {
    transaction(AbyssContext.db) {

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq newGuildName.lowercase()
        }.firstOrNull()

        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]



        if (guild != null){
            player?.error("There is already a guild registered with this name!")
            return@transaction
        }

        Guilds.update({Guilds.id eq guildId}) {
            it[name] = newGuildName
        }

        val guildName = player?.getGuildName()

        /* Message to all guild-members */
        Players.select {
            (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId)
        }.forEach { row ->
            val changedNameMessage =
                "${ChatColor.YELLOW}The Guild you were in has been renamed to ${ChatColor.GOLD}${ChatColor.ITALIC}${guildName}"
            val player = Bukkit.getPlayer(row[Players.playerUUID])
            if (player != null) {
                player.sendMessage(changedNameMessage)
            } else {
                MessageQueue.insert {
                    it[content] = changedNameMessage
                    it[playerUUID] = row[Players.playerUUID]
                }
            }
        }
        player?.success("Your guild was successfully renamed to ${ChatColor.GOLD}${ChatColor.ITALIC}${guildName}!")
    }
}

fun Player.changeGuildJoinType() {
    transaction(AbyssContext.db) {
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val type = Guilds.select {
            Guilds.id eq guildId
        }.single()[Guilds.joinType]

        val newType = when (type) {
            GuildJoinType.Any -> GuildJoinType.Request
            GuildJoinType.Invite -> GuildJoinType.Any
            GuildJoinType.Request -> GuildJoinType.Invite
        }

        Guilds.update({Guilds.id eq guildId}) {
            it[joinType] = newType
        }
    }
}

fun Player.getGuildMembers() : List<Pair<GuildRanks, OfflinePlayer>>  {
    return transaction(AbyssContext.db) {
        val playerRow = Players.select {
            Players.playerUUID eq player!!.uniqueId
        }.single()

        val guildId = playerRow[Players.guildId]

        Players.select {
            (Players.guildId eq guildId)
        }.map { row ->
            Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
}

fun String.getGuildMembers() : List<Pair<GuildRanks, OfflinePlayer>> {
    return transaction(AbyssContext.db) {
        val guild = Guilds.select {
            Guilds.name.lowerCase() eq this@getGuildMembers.lowercase()
        }.singleOrNull()?.get(Guilds.id) ?: return@transaction emptyList<Pair<GuildRanks, OfflinePlayer>>()

        Players.select {
            (Players.guildId eq guild)
        }.map { row ->
            Pair(row[Players.guildRank], Bukkit.getOfflinePlayer(row[Players.playerUUID]))
        }
    }
}

fun getAllGuilds() : List<Triple<String, GuildJoinType, Int>> {
    return transaction(AbyssContext.db) {
        return@transaction Guilds.selectAll().map {row -> Triple(row[Guilds.name], row[Guilds.joinType], row[Guilds.level]) }
    }
}

fun displayGuildList(): List<Triple<String, GuildJoinType, Int>> {
    val list = getAllGuilds().sortedBy { it.third; it.first.getOwnerFromGuildName().getGuildMemberCount(); it.second; it.first }

    return if (list.size < 20) list.subList(0, list.size)
    else list.subList(0, 20)
}

fun String.getOwnerFromGuildName() : OfflinePlayer {
    return transaction(AbyssContext.db) {
        val guild = Guilds.select {
            Guilds.name eq this@getOwnerFromGuildName
        }.first()[Guilds.id]

        val player = Players.select {
            Players.guildId eq guild
        }.first()[Players.playerUUID]

        return@transaction Bukkit.getOfflinePlayer(player)
    }
}
