package com.mineinabyss.mineinabyss.extensions

import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.data.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Player.addMemberToGuild(member: OfflinePlayer) {
    transaction {

        val id = Guilds.select {
            (Players.playerUUID eq uniqueId) and (Players.guildId eq Guilds.id)
        }.single()[Guilds.id]

        val memberGuildCheck = Players.select {
            Players.playerUUID eq member.uniqueId
        }.firstOrNull()?.get(Players.guildId)

        if (memberGuildCheck != null ) {
            player?.error("This player is already in another guild.")
            return@transaction
        }

        //TODO implement max membercount check
//        if () {
//            player?.error("Your guild is full!")
//            return@transaction
//        }


        Players.insert {
            it[playerUUID] = member.uniqueId
            it[guildId] = id
            it[guildRank] = GuildRanks.Member
        }
        player?.success("$member joined your Guild!")

        val joinMessage = "${ChatColor.GREEN}You have been accepted into ${player?.getGuildName()}"
        if (member.isOnline) member.player?.success(joinMessage)
        else {
            MessageQueue.insert {
                it[content] = joinMessage
                it[playerUUID] = member.uniqueId
            }
        }
    }
}

fun Player.invitePlayerToGuild(invitedPlayer: String) {
    val invitedMember = Bukkit.getOfflinePlayer(invitedPlayer)
    val inviteMessage =
        "${ChatColor.YELLOW}You have been invited to join the ${ChatColor.GOLD}${player?.getGuildName()} ${ChatColor.YELLOW}guild."
    transaction {
        /* Should invites be cancelled if player already is in one? */
        /* Or should this be checked when a player tries to accept an invite? */
        if (invitedMember.hasGuild()) {
            player?.error("This player is already in another guild!")
            return@transaction
        }

        GuildJoinQueue.insert {
            it[playerUUID] = invitedMember.uniqueId
            it[guildId] = guildId
            it[joinType] = GuildJoinType.Invite
        }

        /* Adds invitedPlayer into the guild of the player. */
        //player?.addMemberToGuild(invitedMember)
        player?.success("Player was invited to your guild!")

        if (invitedMember.isOnline) invitedMember.player?.success(inviteMessage)
        else {
            MessageQueue.insert {
                it[content] = inviteMessage
                it[playerUUID] = invitedMember.uniqueId
            }
        }
    }
}

fun Player.lookForGuild(guildName: String) {
    val player = player!!
    val requestMessage = "${ChatColor.GREEN}The Guild will receive your request!"
    val ownerMessage = "${ChatColor.GOLD}${ChatColor.ITALIC}${player.name} ${ChatColor.YELLOW}requested to join your guild."

    transaction {
        if (player.hasGuild()){
            player.error("You cannot look for other guilds whilst you are a member of another.")
            return@transaction
        }

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.firstOrNull()

        val id = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.single()[Guilds.id]

        val oldRequest = GuildJoinQueue.select {
            (GuildJoinQueue.playerUUID eq uniqueId) and (GuildJoinQueue.guildId eq id)
        }.firstOrNull()


        if (oldRequest != null) {
            player.error("You have already made a request to this guild, please await their decision.")
            return@transaction
        }

        if (guild == null) {
            player.error("There is no guild with the name $guildName.")
            return@transaction
        }

        GuildJoinQueue.insert {
            it[playerUUID] = player.uniqueId
            it[guildId] = id
            it[joinType] = GuildJoinType.Request
        }

        /* Check if guild is in invite-only mode */
        if (guild[Guilds.joinType] == GuildJoinType.Invite) {
            player.error("${ChatColor.GOLD}$guildName ${ChatColor.YELLOW}is invite-only.")
            return@transaction
        }

        val owner = Players.select {
            (Players.guildId eq id) and
            ((Players.guildRank eq GuildRanks.Owner) or (Players.guildRank eq GuildRanks.Captain))
        }.single()[Players.playerUUID]


        if (owner.toPlayer()?.isOnline == true) {
            owner.toPlayer()?.sendMessage(ownerMessage)
        }
        else {
            MessageQueue.insert {
                it[content] = ownerMessage
                it[playerUUID] = owner
            }
        }


        if (player.isOnline) {
            player.success(requestMessage)
            return@transaction
        }
        else {
            MessageQueue.insert {
                it[content] = requestMessage
                it[playerUUID] = uniqueId
            }
            return@transaction
        }
    }
}

/* Promote a guildmembers rank in your Guild */
fun Player.promotePlayerInGuild(member: OfflinePlayer) {

    transaction {
        val playerRow = Players.select {
            Players.playerUUID eq member.uniqueId
        }.single()

        //TODO Make memberRank not be int, but show rank name
        val memberRank = playerRow[Players.guildRank]
        val promoteMessage = "${ChatColor.GREEN}You have been promoted to $memberRank in ${ChatColor.ITALIC}${player?.getGuildName()}"

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

        Players.deleteWhere {
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

fun Player.leaveGuild() {
    transaction {
        val playerRow = Players.select {
            Players.playerUUID eq uniqueId
        }.single()

        val memberRank = playerRow[Players.guildRank]
        val guildName = player?.getGuildName()

        /* Deletes player-entry if player has a guild */
        if (memberRank == GuildRanks.Owner) {
            player?.error("You have to promote another member to Owner before leaving your guild.")
            return@transaction
        }

        //TODO Check if player is last one left. If so prompt disbanding instead

        Players.deleteWhere {
            Players.playerUUID eq uniqueId
        }
        player?.success("You have left ${ChatColor.ITALIC}${guildName}")
    }

}

fun Player.guildRank() : String {
    return transaction {
        val rank = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()?.get(Players.guildRank)

        if (rank != null) return@transaction rank; else return@transaction ""
    }
}

fun OfflinePlayer.hasGuild() : Boolean {
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

fun Player.getGuildLevel() : Int {
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

