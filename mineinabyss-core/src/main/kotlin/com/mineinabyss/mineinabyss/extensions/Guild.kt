package com.mineinabyss.mineinabyss.extensions

import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.data.Guilds
import com.mineinabyss.mineinabyss.data.MessageQueue
import com.mineinabyss.mineinabyss.data.Players
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
            player?.error("There is already a guild registered with this name!")
            return@transaction
        }
        else player?.success("Your Guild has been registered with the name ${ChatColor.ITALIC}$guildName")

        val rowID = Guilds.insert {
            it[name] = guildName
            it[balance] = 0
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

        if (player?.guildRank() != GuildRanks.Owner) {
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
        broadcast("test4")
        //return@transaction
    }
}

fun changeStoredGuildName(newGuildName: String) {
    transaction {
        Guilds.update {
            it[name] = newGuildName
        }
    }
}

fun OfflinePlayer.notifyGuildRename() {
    transaction {
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        /* Message to all guild-members */
        Players.select {
            (Players.guildId eq guildId) and (Players.playerUUID neq uniqueId)
        }.forEach { row ->
            val changedNameMessage =
                "${ChatColor.YELLOW}The Guild you were in has been renamed to ${ChatColor.GOLD}${ChatColor.ITALIC}${Guilds.name}"
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
    }
}


