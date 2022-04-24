package com.mineinabyss.mineinabyss.extensions

import com.mineinabyss.guilds.database.*
import com.mineinabyss.helpers.MessageQueue
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.mineinabyss.core.AbyssContext
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun OfflinePlayer.addMemberToGuild(member: OfflinePlayer) {
    transaction(AbyssContext.db) {
        val guild = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()?.get(Players.guildId)

        if (guild == null) {
            player?.error("You cannot add a player when you have no guild.")
            return@transaction
        }

        val guildRow = Guilds.select {
            Guilds.id eq guild
        }.single()

        val id = guildRow[Guilds.id]
        val level = guildRow[Guilds.level]
        val joinType = guildRow[Guilds.joinType]

        val memberGuildCheck = Players.select {
            Players.playerUUID eq member.uniqueId
        }.firstOrNull()?.get(Players.guildId)

        if (memberGuildCheck == id) {
            player?.error("This player is already in your guild.")
            return@transaction
        }

        if (memberGuildCheck != null) {
            player?.error("This player is already in another guild.")
            return@transaction
        }

        val maxGuildMemberCount = level * 5 // Members per level

        /* > and not >= to not count Guild Owner */
        if (player!!.getGuildMemberCount() > maxGuildMemberCount) {
            player?.error("Your guild is full!")
            return@transaction
        }


        Players.insert {
            it[playerUUID] = member.uniqueId
            it[guildId] = id
            it[guildRank] = GuildRanks.Member
        }
        player?.success("${member.name} joined your Guild!")

        val joinMessage = "${ChatColor.GREEN}You have been accepted into ${player?.getGuildName()}"
        if (member.isOnline) member.player?.success(joinMessage)
        else {
            MessageQueue.insert {
                it[content] = joinMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Delete accepted guildinvite */
        GuildJoinQueue.deleteWhere {
            (GuildJoinQueue.playerUUID eq member.uniqueId) and (GuildJoinQueue.guildId eq guild)
        }
    }
}

fun OfflinePlayer.invitePlayerToGuild(invitedPlayer: String) {
    // TODO send message when player not found
    val invitedMember = Bukkit.getOfflinePlayerIfCached(invitedPlayer) ?: return
    val inviteMessage =
        "${ChatColor.YELLOW}You have been invited to join the ${ChatColor.GOLD}${this@invitePlayerToGuild.getGuildName()} ${ChatColor.YELLOW}guild."
    transaction(AbyssContext.db) {
        /* Should invites be cancelled if player already is in one? */
        /* Or should this be checked when a player tries to accept an invite? */
        if (invitedMember.hasGuild()) {
            player?.error("This player is already in another guild!")
            return@transaction
        }

        if (invitedMember.hasGuildInvite(this@invitePlayerToGuild)) {
            player?.error("This player has already been invited to your guild!")
            return@transaction
        }

        //val owner = (invitedMember as Player).getGuildOwnerFromInvite().toPlayer()
        if (player?.hasGuildRequest() == true) {
            player?.error("This player has already requested to join your guild!")
            player?.error("Navigate to the ${ChatColor.BOLD}Manage Join Request" + "menu to respond.")
            return@transaction
        }

        val id = Players.select {
            Players.playerUUID eq uniqueId
        }.singleOrNull()?.get(Players.guildId) ?: return@transaction

        val guild = Guilds.select {
            Guilds.id eq id
        }.single()[Guilds.id]

        GuildJoinQueue.insert {
            it[playerUUID] = invitedMember.uniqueId
            it[guildId] = guild
            it[joinType] = GuildJoinType.Invite
        }

        /* Adds invitedPlayer into the guild of the player. */
        player?.success("${invitedMember.name} was invited to your guild!")

        if (invitedMember.isOnline) invitedMember.player?.success(inviteMessage)
        else {
            MessageQueue.insert {
                it[content] = inviteMessage
                it[playerUUID] = invitedMember.uniqueId
            }
        }
    }
}

fun OfflinePlayer.verifyGuildName(guildName: String) : String? {
    return transaction(AbyssContext.db) {

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.firstOrNull()

        if (guild == null) {
            player?.error("There is no guild with the name ${ChatColor.DARK_RED}${ChatColor.ITALIC}$guildName.")
            return@transaction null
        }

        return@transaction guildName
    }
}

fun OfflinePlayer.requestToJoin(guildName: String) {
    val player = player ?: return
    val requestMessage = "${ChatColor.GREEN}The Guild will receive your request!"
    val ownerMessage =
        "${ChatColor.GOLD}${ChatColor.ITALIC}${player.name} ${ChatColor.YELLOW}requested to join your guild."

    transaction(AbyssContext.db) {
        if (player.hasGuild()) {
            player.error("You cannot look for other guilds whilst you are a member of another.")
            return@transaction
        }

        val guild = Guilds.select {
            Guilds.name.lowerCase() eq guildName.lowercase()
        }.firstOrNull()

        if (guild == null) {
            player.error("There is no guild with the name ${ChatColor.DARK_RED}${ChatColor.ITALIC}$guildName.")
            return@transaction
        }

        /* Check if guild is in invite-only mode */
        if (guild[Guilds.joinType] == GuildJoinType.Invite) {
            player.error("${ChatColor.GOLD}$guildName ${ChatColor.YELLOW}is invite-only.")
            return@transaction
        }

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


        GuildJoinQueue.insert {
            it[playerUUID] = player.uniqueId
            it[guildId] = id
            it[joinType] = GuildJoinType.Request
        }

        val owner = Players.select {
            (Players.guildId eq id) and (Players.guildRank eq GuildRanks.Owner)
        }.single()[Players.playerUUID]


        if (owner.toPlayer()?.isOnline == true) {
            owner.toPlayer()?.sendMessage(ownerMessage)
        } else {
            MessageQueue.insert {
                it[content] = ownerMessage
                it[playerUUID] = owner
            }
        }


        if (player.isOnline) {
            player.success(requestMessage)
            return@transaction
        } else {
            MessageQueue.insert {
                it[content] = requestMessage
                it[playerUUID] = uniqueId
            }
            return@transaction
        }
    }
}

/* Promote a guildmembers rank in your Guild */
fun OfflinePlayer.promotePlayerInGuild(member: OfflinePlayer) {

    transaction(AbyssContext.db) {
        val newRank = when (member.getGuildRank()) {
            GuildRanks.Owner -> {
                player?.error("You cannot be promoted to a higher rank!")
                return@transaction
            }
            GuildRanks.Captain -> {
                player?.error("You cannot be promoted to Owner, as there is already one.")
                return@transaction
            }
            GuildRanks.Steward -> GuildRanks.Captain
            GuildRanks.Member -> GuildRanks.Steward
            else -> return@transaction
        }

        /* Send message if online, otherwise queue it */
        //playerRow[Players.guildRank]
        Players.update({ Players.playerUUID eq member.uniqueId }) {
            it[guildRank] = newRank
        }

        val promoteMessage =
            "${ChatColor.GREEN}You have been promoted to ${member.getGuildRank()} in ${ChatColor.ITALIC}${player?.getGuildName()}"

        if (member.isOnline) member.player?.success(promoteMessage)
        else {
            MessageQueue.insert {
                it[content] = promoteMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Message owner */
        this@promotePlayerInGuild.player?.success("You have promoted ${member.name} to ${ChatColor.ITALIC}${member.getGuildRank()}")

        return@transaction
    }
}

/* Kicks a player from guild */
fun OfflinePlayer.kickPlayerFromGuild(member: OfflinePlayer): Boolean {

    return transaction(AbyssContext.db) {
        val dbPlayer = Players
            .select { Players.playerUUID eq member.uniqueId }
            .firstOrNull() ?: return@transaction true

        val executor = Players
            .select { Players.playerUUID eq uniqueId }
            .firstOrNull() ?: return@transaction true

        val playerRow = Players.select {
            Players.playerUUID eq uniqueId
        }.single()

        val memberRank = playerRow[Players.guildRank]
        val guildID = playerRow[Players.guildId]

        /* Check if kicker is in same guild as kicked */
        if (executor[Players.guildId] != guildID) {
            return@transaction false
        }

        /* Check role of kicker */
        if (dbPlayer[Players.guildRank] <= memberRank) {
            player?.error("You cannot kick someone with the same or higher rank than you!")
            return@transaction false
        }

        Players.deleteWhere {
            (Players.playerUUID eq member.uniqueId) and
                    (Players.guildId eq guildID)
        }

        /* Message to actual kicked player online or offline */
        val kickMessage = "You have been kicked from ${ChatColor.ITALIC}${player?.getGuildName()}"
        if (member.isOnline) member.player?.error(kickMessage)
        else {
            MessageQueue.insert {
                it[content] = kickMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Message owner */
        player?.success("You have kicked ${ChatColor.ITALIC}${member.name} from ${ChatColor.ITALIC}${player?.getGuildName()}!")

        return@transaction true
    }
}

fun Player.leaveGuild() {
    transaction(AbyssContext.db) {
        val playerRow = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()

        if (playerRow == null) {
            player?.error("You cannot leave a guild when you're not a member of any!")
            return@transaction
        }

        val memberRank = playerRow[Players.guildRank]
        val guildName = player?.getGuildName() ?: return@transaction

        /* Deletes player-entry if player has a guild */
        if (memberRank == GuildRanks.Owner) {
            player?.error("You have to promote another member to Owner before leaving your guild.")
            return@transaction
        }

        Players.deleteWhere {
            Players.playerUUID eq uniqueId
        }
        player?.success("You have left ${ChatColor.ITALIC}${guildName}")
    }

}

fun OfflinePlayer.getGuildRank(): GuildRanks? {
    return transaction(AbyssContext.db) {
        val rank = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()?.get(Players.guildRank)

        if (rank != null) return@transaction rank; else return@transaction null
    }
}

fun OfflinePlayer.hasGuild(): Boolean {
    return transaction(AbyssContext.db) {
        val hasGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.firstOrNull()
        return@transaction hasGuild !== null
    }
}

fun OfflinePlayer.getGuildName(): String {
    return transaction(AbyssContext.db) {
        val playerGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val guildName = Guilds.select {
            Guilds.id eq playerGuild
        }.single()[Guilds.name]
        return@transaction guildName
    }
}

fun OfflinePlayer.getGuildOwner() : UUID {
    return transaction(AbyssContext.db) {
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

fun OfflinePlayer.isGuildOwner() : Boolean {
    return player?.getGuildOwner() == player?.uniqueId
}

fun OfflinePlayer.getGuildOwnerFromInvite() : UUID {
    return transaction(AbyssContext.db) {
        val guilds = GuildJoinQueue.select {
            (GuildJoinQueue.playerUUID eq player!!.uniqueId) and (GuildJoinQueue.joinType eq GuildJoinType.Invite)
        }.singleOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction player?.uniqueId!!

        return@transaction Players.select {
            (Players.guildId eq guilds) and (Players.guildRank eq GuildRanks.Owner)
        }.single()[Players.playerUUID]
    }
}

fun OfflinePlayer.getGuildLevel(): Int? {
    return transaction(AbyssContext.db) {
        val playerGuild = (Players.select {
            Players.playerUUID eq uniqueId
        }.singleOrNull() ?: return@transaction null)[Players.guildId]

        val guildLevel = Guilds.select {
            Guilds.id eq playerGuild
        }.single()[Guilds.level]
        return@transaction guildLevel
    }
}

fun String.getGuildLevel(): Int {
    return transaction(AbyssContext.db) {
        return@transaction Guilds.select {
            Guilds.name.lowerCase() eq this@getGuildLevel.lowercase()
        }.singleOrNull()?.get(Guilds.level) ?: return@transaction 0
    }
}

fun OfflinePlayer.getGuildMemberCount(): Int {
    return transaction(AbyssContext.db) {
        val playerRow = Players.select {
            Players.playerUUID eq uniqueId
        }.first()[Players.guildId]

        val members = Players.select {
            Players.guildId eq playerRow
        }.map { row ->
            Bukkit.getOfflinePlayer(row[Players.playerUUID])
        }

        return@transaction members.count()
    }
}

fun OfflinePlayer.hasGuildInvite(guildOwner: OfflinePlayer): Boolean {
    return transaction(AbyssContext.db) {
        val joinQueueId = GuildJoinQueue.select {
            (GuildJoinQueue.playerUUID eq uniqueId) and (GuildJoinQueue.joinType eq GuildJoinType.Invite)
        }.singleOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction false

        val ownerId = Players.select {
            Players.playerUUID eq guildOwner.uniqueId
        }.single()[Players.guildId]

        val hasInvite = Guilds.select {
            (Guilds.id eq joinQueueId) and (Guilds.id eq ownerId)
        }.firstOrNull()

        return@transaction hasInvite !== null
    }
}

fun OfflinePlayer.hasGuildInvites(): Boolean {
    return transaction(AbyssContext.db) {
        return@transaction GuildJoinQueue.select {
            (GuildJoinQueue.joinType eq GuildJoinType.Invite) and (GuildJoinQueue.playerUUID eq uniqueId)
        }.toList().isNotEmpty()
    }
}

fun OfflinePlayer.hasGuildRequests(): Boolean {
    return transaction(AbyssContext.db) {
        val player = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        return@transaction GuildJoinQueue.select {
            (GuildJoinQueue.joinType eq GuildJoinType.Request) and (GuildJoinQueue.guildId eq player)
        }.toList().isNotEmpty()
    }
}

fun OfflinePlayer.hasGuildRequest(): Boolean {
    return transaction(AbyssContext.db) {
        val player = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val joinQueueId = GuildJoinQueue.select {
            (GuildJoinQueue.guildId eq player) and (GuildJoinQueue.joinType eq GuildJoinType.Request)
        }.firstOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction false

        val hasRequest = Guilds.select {
            Guilds.id eq joinQueueId
        }.firstOrNull()

        return@transaction hasRequest !== null
    }
}

fun  OfflinePlayer.getNumberOfGuildRequests() : Int {
    var requestCount = 0
    val amount = transaction(AbyssContext.db) {
        val playerGuild = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        GuildJoinQueue.select {
            (GuildJoinQueue.guildId eq playerGuild) and (GuildJoinQueue.joinType eq GuildJoinType.Request)
        }.map { row -> row[GuildJoinQueue.guildId] }.forEach { _ ->
            requestCount++
        }
        return@transaction requestCount
    }
    return amount
}

fun OfflinePlayer.removeGuildQueueEntries(guildJoinType: GuildJoinType, removeAll: Boolean = false) {
    return transaction(AbyssContext.db) {
        val id = GuildJoinQueue.select {
            GuildJoinQueue.playerUUID eq uniqueId
        }.singleOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction

        if (removeAll) {
            GuildJoinQueue.deleteWhere {
                (GuildJoinQueue.joinType eq guildJoinType) and
                (GuildJoinQueue.guildId eq id)
            }
        }
        else {
            GuildJoinQueue.deleteWhere {
                (GuildJoinQueue.playerUUID eq uniqueId) and
                (GuildJoinQueue.joinType eq guildJoinType) and
                (GuildJoinQueue.guildId eq id)
            }
        }
    }
}

fun OfflinePlayer.getGuildJoinType(): GuildJoinType {
    val joinType =  transaction(AbyssContext.db) {
        val guildId = Players.select {
            Players.playerUUID eq uniqueId
        }.single()[Players.guildId]

        val type: GuildJoinType = Guilds.select {
            Guilds.id eq guildId
        }.single()[Guilds.joinType]

        return@transaction type
    }
    return joinType
}

fun OfflinePlayer.isAboveCaptain(): Boolean {
    return (player?.getGuildRank() == GuildRanks.Owner || player?.getGuildRank() == GuildRanks.Captain)
}
