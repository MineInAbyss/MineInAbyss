package com.mineinabyss.mineinabyss.extensions

import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.data.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Player.createGuild(guildName: String) {
    transaction {

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
    transaction {
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
    transaction {

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
    transaction {
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

