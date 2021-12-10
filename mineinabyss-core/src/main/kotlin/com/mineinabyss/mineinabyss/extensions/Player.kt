package com.mineinabyss.mineinabyss.extensions

import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.data.GuildRanks
import com.mineinabyss.mineinabyss.data.Guilds
import com.mineinabyss.mineinabyss.data.MessageQueue
import com.mineinabyss.mineinabyss.data.Players
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun OfflinePlayer.acceptPlayerToGuild(player: Player, guildID: Int) {
    val playerUUID = uniqueId

    transaction {
        val playerRow = Players
            .select { Players.playerUUID eq playerUUID }
            .firstOrNull()

        val guildId = Players
            .select { Players.guildId eq guildID }
            .firstOrNull()

        this@acceptPlayerToGuild.player?.success("${player.name} joined your Guild!")
        player.error("You have been accepted into ${ChatColor.ITALIC}${Guilds.name}")

    }
}

fun OfflinePlayer.invitePlayerToGuild(invitedPlayer: Player) {

    transaction {
        /* Give player an invite to join the Guild */
        /* if accepts, fire acceptPlayerToGuild? */
    }
}

/* Promote a guildmembers rank in your Guild */
fun Player.promotePlayerInGuild(member: OfflinePlayer) {

    transaction {
        val playerRow = Players.select {
            Players.playerUUID eq member.uniqueId
        }.single()

        val memberRank = playerRow[Players.guildRank]
        val promoteMessage = "You have been promoted to $memberRank in ${ChatColor.ITALIC}${Guilds.name}"

        if (memberRank == GuildRanks.Owner){
            return@transaction true
        }

        if (memberRank == GuildRanks.Captain) {
            player?.error("You cannot be promoted to Owner, as there is already one.")
        }

        /* Send message if online, otherwise queue it */
        else {
            memberRank + 1
            if (member.isOnline) member.player?.success(promoteMessage)
            else {
                MessageQueue.insert {
                    it[content] = promoteMessage
                    it[playerUUID] = member.uniqueId
                }
            }

        }
    }
}

/* Kicks a player from guild */
fun Player.kickPlayerFromGuild(member: OfflinePlayer) : Boolean {

    return transaction{
        val dbPlayer = Players
            .select{Players.playerUUID eq member.uniqueId}
            .firstOrNull() ?: return@transaction true

        val executor = Players
            .select{Players.playerUUID eq uniqueId}
            .firstOrNull() ?: return@transaction true

        val playerRow = Players.select {
            Players.playerUUID eq uniqueId
        }.single()

        val memberRank = playerRow[Players.guildRank]
        val guildID = playerRow[Players.guildId]

        /* Check if kicker is in same guild as kicked */
        if (executor[Players.guildId] != guildID){
            return@transaction false
        }

         /* Check role of kicker */
        if (dbPlayer[Players.guildRank] >= memberRank) {
            player?.error("You cannot kick someone with the same or higher rank than you!")
            return@transaction false
        }

        val deleteCode = Players.deleteWhere {
            (Players.playerUUID eq member.uniqueId) and
                    (Players.guildId eq guildID)
        }

        /* Message to actual kicked player online or offline */
        val kickMessage = "You have been kicked from ${ChatColor.ITALIC}${Guilds.name}"
        if (member.isOnline) member.player?.error(kickMessage)
        else {
            MessageQueue.insert {
                it[content] = kickMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Message owner */
        this@kickPlayerFromGuild.player?.success("You have kicked ${player?.name} from ${ChatColor.ITALIC}${Guilds.name}")

        return@transaction true
    }
}

//TODO Fix: java.lang.IllegalStateException: No transaction in context.
fun Player.leaveGuild() {
    transaction {
        val playerRow = Players.select {
            Players.playerUUID eq uniqueId
        }.single()

        val memberRank = playerRow[Players.guildRank]
        val guildName = player?.getGuildName()

        /* Deletes player-entry if player has a guild */
        if (memberRank != GuildRanks.Owner) {
            player?.error("You have to promote another member to Owner before leaving your guild.")
        }
        else {
            Players.deleteWhere {
                Players.playerUUID eq uniqueId
            }
            player?.success("You have left ${ChatColor.ITALIC}${guildName}")
        }
    }

}

fun Player.guildRank() : Int? {
    return transaction {
        val rank = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()?.get(Players.guildRank)

        if (rank != null) return@transaction rank; else return@transaction null
    }
}

fun Player.hasGuild() : Boolean {
    return transaction {
        val hasGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()
        return@transaction hasGuild !== null
    }
}

fun Player.getGuildName() : String{
    return transaction {
        val playerGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val guildName = Guilds.select {
            Guilds.id eq playerGuild
        }.single()[Guilds.name]
        return@transaction guildName
    }
}

fun Player.getGuildOwner() : UUID {
    return transaction {
        val playerGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val guildId = Guilds.select {
            Guilds.id eq playerGuild
        }.single()[Guilds.id]

        val guildOwner = Players.select {
            (Players.guildId eq guildId) and (Players.guildRank eq GuildRanks.Owner)
        }.single()[Players.playerUUID]
        return@transaction guildOwner
    }
}

fun Player.getGuildLevel() : Int{
    return transaction {
        val playerGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val guildLevel = Guilds.select {
            Guilds.id eq playerGuild
        }.single()[Guilds.level]
        return@transaction guildLevel
    }
}

