package com.mineinabyss.features.guilds.extensions

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.chatty.components.ChannelData
import com.mineinabyss.chatty.helpers.defaultChannel
import com.mineinabyss.features.abyss
import com.mineinabyss.features.guilds.database.*
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.guiy.inventory.GuiyInventoryHolder
import com.mineinabyss.idofront.entities.toPlayer
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.serialize
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
import java.util.*

fun UUID.toOfflinePlayer(): OfflinePlayer = Bukkit.getOfflinePlayer(this)

fun OfflinePlayer.addMemberToGuild(member: OfflinePlayer): Boolean {
    var added = false
    transaction(abyss.db) {
        val guild = Players.selectAll().where { Players.playerUUID eq uniqueId }.firstOrNull()?.get(Players.guildId)

        if (guild == null) {
            player?.error("You cannot add a player when you have no guild.")
            return@transaction
        }

        val guildRow = Guilds.selectAll().where { Guilds.id eq guild }.single()

        val id = guildRow[Guilds.id]
        val level = guildRow[Guilds.level]

        val memberGuildCheck =
            Players.selectAll().where { Players.playerUUID eq member.uniqueId }.firstOrNull()?.get(Players.guildId)

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
        if (getGuildMemberCount() > maxGuildMemberCount) {
            player?.error("Your guild is full!")
            return@transaction
        }


        Players.insert {
            it[playerUUID] = member.uniqueId
            it[guildId] = id
            it[guildRank] = GuildRank.MEMBER
        }
        player?.success("${member.name} joined your Guild!")
        added = true

        val joinMessage = "<green>You have been accepted into ${player?.getGuildName()}"
        if (member.isOnline) member.player?.success(joinMessage)
        else {
            GuildMessageQueue.insert {
                it[content] = joinMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Delete accepted guildinvite */
        GuildJoinQueue.deleteWhere {
            (playerUUID eq member.uniqueId) and (guildId eq guild)
        }
    }

    return added
}

fun OfflinePlayer.invitePlayerToGuild(invitedPlayer: String) {
    // TODO send message when player not found
    val invitedMember = Bukkit.getOfflinePlayerIfCached(invitedPlayer) ?: return
    val inviteMessage =
        "<yellow>You have been invited to join the <gold>${this@invitePlayerToGuild.getGuildName()}</gold> guild."
    transaction(abyss.db) {
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
            player?.error("Navigate to the <b>Manage GuildJoin REQUEST</b> menu to respond.")
            return@transaction
        }

        val id = Players.selectAll().where { Players.playerUUID eq uniqueId }.singleOrNull()?.get(Players.guildId)
            ?: return@transaction

        val guild = Guilds.selectAll().where { Guilds.id eq id }.single()[Guilds.id]

        GuildJoinQueue.insert {
            it[playerUUID] = invitedMember.uniqueId
            it[guildId] = guild
            it[joinType] = GuildJoinType.INVITE
        }

        /* Adds invitedPlayer into the guild of the player. */
        player?.success("${invitedMember.name} was invited to your guild!")

        if (invitedMember.isOnline) invitedMember.player?.success(inviteMessage)
        else {
            GuildMessageQueue.insert {
                it[content] = inviteMessage
                it[playerUUID] = invitedMember.uniqueId
            }
        }
    }
}

fun OfflinePlayer.verifyGuildName(guildName: String): String? {
    return transaction(abyss.db) {

        val guild = Guilds.selectAll().where { Guilds.name.lowerCase() eq guildName.lowercase() }.firstOrNull()

        if (guild == null) {
            player?.error("There is no guild with the name <dark_red><i>$guildName</i></dark_red>.")
            return@transaction null
        }

        return@transaction guildName
    }
}

fun OfflinePlayer.requestToJoin(guildName: String) {
    val player = player ?: return
    val requestMessage = "The Guild will receive your request!"
    val ownerMessage = "<gold><i>${player.name}</i> <yellow>requested to join your guild.".miniMsg()

    transaction(abyss.db) {
        if (player.hasGuild()) {
            player.error("You cannot look for other guilds whilst you are a member of another.")
            return@transaction
        }

        val guild = Guilds.selectAll().where { Guilds.name.lowerCase() eq guildName.lowercase() }.firstOrNull()

        if (guild == null) {
            player.error("There is no guild with the name <dark_red><i>$guildName</i></dark_red>.")
            return@transaction
        }

        /* Check if guild is in invite-only mode */
        if (guild[Guilds.joinType] == GuildJoinType.INVITE) {
            player.error("<gold>$guildName <yellow>is invite-only.")
            return@transaction
        }

        val id = Guilds.selectAll().where { Guilds.name.lowerCase() eq guildName.lowercase() }.single()[Guilds.id]

        val oldRequest = GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.playerUUID eq uniqueId) and (GuildJoinQueue.guildId eq id) }.firstOrNull()

        if (oldRequest != null) {
            player.error("You have already made a request to this guild, please await their decision.")
            return@transaction
        }


        GuildJoinQueue.insert {
            it[playerUUID] = player.uniqueId
            it[guildId] = id
            it[joinType] = GuildJoinType.REQUEST
        }

        val owner = Players.selectAll().where { (Players.guildId eq id) and (Players.guildRank eq GuildRank.OWNER) }
            .single()[Players.playerUUID]


        if (owner.toPlayer()?.isOnline == true) {
            owner.toPlayer()?.sendMessage(ownerMessage)
        } else {
            GuildMessageQueue.insert {
                it[this.content] = ownerMessage.serialize()
                it[playerUUID] = owner
            }
        }


        if (player.isOnline) {
            player.success(requestMessage)
            return@transaction
        } else {
            GuildMessageQueue.insert {
                it[content] = requestMessage
                it[playerUUID] = uniqueId
            }
            return@transaction
        }
    }
}

/* Promote a guildmembers rank in your Guild */
fun OfflinePlayer.promotePlayerInGuild(member: OfflinePlayer) {

    transaction(abyss.db) {
        val newRank = when (member.getGuildRank()) {
            GuildRank.OWNER -> {
                player?.error("You cannot be promoted to a higher rank!")
                return@transaction
            }

            GuildRank.CAPTAIN -> {
                player?.error("You cannot be promoted to Owner, as there is already one.")
                return@transaction
            }

            GuildRank.STEWARD -> GuildRank.CAPTAIN
            GuildRank.MEMBER -> GuildRank.STEWARD
            else -> return@transaction
        }

        /* Send message if online, otherwise queue it */
        //playerRow[Players.guildRank]
        Players.update({ Players.playerUUID eq member.uniqueId }) {
            it[guildRank] = newRank
        }

        val promoteMessage =
            "<aqua>You have been promoted to ${member.getGuildRank()} in <i>${player?.getGuildName()}"

        if (member.isOnline) member.player?.success(promoteMessage)
        else {
            GuildMessageQueue.insert {
                it[content] = promoteMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Message owner */
        this@promotePlayerInGuild.player?.success("You have promoted ${member.name} to <i>${member.getGuildRank()}")

        return@transaction
    }
}

fun OfflinePlayer.setGuildRank(member: OfflinePlayer, rank: GuildRank) {
    transaction(abyss.db) {
        if (member.isGuildOwner()) {
            player?.error("You cannot change the rank of the guild owner.")
            return@transaction
        }

        /* Send message if online, otherwise queue it */
        //playerRow[Players.guildRank]
        Players.update({ Players.playerUUID eq member.uniqueId }) {
            it[guildRank] = rank
        }

        val promoteMessage =
            "<aqua>Your guild rank in <i>${player?.getGuildName()}</i> has been set to <i>${rank.name}</i>"

        if (member.isOnline) member.player?.success(promoteMessage)
        else {
            GuildMessageQueue.insert {
                it[content] = promoteMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Message owner */
        this@setGuildRank.player?.success("${member.name}'s rank has been changed to <i>${rank}")

        return@transaction
    }
}

/* Kicks a player from guild */
fun OfflinePlayer.kickPlayerFromGuild(member: OfflinePlayer): Boolean {

    return transaction(abyss.db) {
        val dbPlayer = Players
            .selectAll().where { Players.playerUUID eq member.uniqueId }
            .firstOrNull() ?: return@transaction true

        val executor = Players
            .selectAll().where { Players.playerUUID eq uniqueId }
            .firstOrNull() ?: return@transaction true

        val playerRow = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()

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

        abyss.plugin.launch {
            val gearyPlayer = member.player?.toGeary() ?: return@launch
            val channelData = gearyPlayer.get<ChannelData>() ?: return@launch
            // Remove player from guild-chat. Offline members handled when they join
            if (member.isOnline && channelData.channelId == getGuildName()?.guildChatId()) {
                gearyPlayer.setPersisting(channelData.copy(channelId = defaultChannel().key))
            }
        }

        Players.deleteWhere {
            (playerUUID eq member.uniqueId) and (guildId eq guildID)
        }

        /* Message to actual kicked player online or offline */
        val kickMessage = "<red>You have been kicked from <i>${getGuildName()}"
        if (member.isOnline) {
            member.player?.error(kickMessage)
            if (member.player!!.openInventory.topInventory.holder is GuiyInventoryHolder) member.player?.closeInventory()
        } else {
            GuildMessageQueue.insert {
                it[content] = kickMessage
                it[playerUUID] = member.uniqueId
            }
        }

        /* Message owner */
        player?.success("You have kicked <i>${member.name}</i> from <i>${player?.getGuildName()}</i>!")

        return@transaction true
    }
}

fun Player.leaveGuild() {
    transaction(abyss.db) {
        val playerRow = Players.selectAll().where { Players.playerUUID eq uniqueId }.firstOrNull()

        if (playerRow == null) {
            player?.error("You cannot leave a guild when you're not a member of any!")
            return@transaction
        }

        val memberRank = playerRow[Players.guildRank]
        val guildName = player?.getGuildName() ?: return@transaction
        val owner = guildName.getOwnerFromGuildName()

        /* Deletes player-entry if player has a guild */
        if (memberRank == GuildRank.OWNER) {
            player?.error("You have to promote another member to Owner before leaving your guild.")
            return@transaction
        }

        abyss.plugin.launch {
            val gearyPlayer = this@leaveGuild.player?.toGeary() ?: return@launch
            val channelData = gearyPlayer.get<ChannelData>() ?: return@launch
            if (channelData.channelId == guildName.guildChatId()) {
                gearyPlayer.setPersisting(channelData.copy(channelId = defaultChannel().key))
            }
        }

        Players.deleteWhere {
            playerUUID eq uniqueId
        }
        val leftMessage = "<i>${player?.name}</i> has left your guild!"
        player?.success("You have left <i>${guildName}")
        owner.player?.warn(leftMessage) ?: GuildMessageQueue.insert {
            it[content] = leftMessage; it[playerUUID] = owner.uniqueId
        }
    }

}

fun OfflinePlayer.getGuildRank(): GuildRank? {
    return transaction(abyss.db) {
        val rank = Players.selectAll().where { Players.playerUUID eq uniqueId }.firstOrNull()?.get(Players.guildRank)

        if (rank != null) return@transaction rank; else return@transaction null
    }
}

fun OfflinePlayer.hasGuild(): Boolean {
    return transaction(abyss.db) {
        val hasGuild = Players.selectAll().where { Players.playerUUID eq uniqueId }.firstOrNull()
        return@transaction hasGuild !== null
    }
}

fun OfflinePlayer.getGuildName(): GuildName? {
    return transaction(abyss.db) {
        val playerGuild =
            Players.selectAll().where { Players.playerUUID eq uniqueId }.singleOrNull()?.get(Players.guildId)
                ?: return@transaction null

        val guildName = Guilds.selectAll().where { Guilds.id eq playerGuild }.singleOrNull()?.get(Guilds.name)
            ?: return@transaction null
        return@transaction guildName
    }
}

fun OfflinePlayer.getGuildOwner(): UUID? {
    return transaction(abyss.db) {
        val playerGuild =
            Players.selectAll().where { Players.playerUUID eq uniqueId }.singleOrNull()?.get(Players.guildId)
                ?: return@transaction null

        val guildId = Guilds.selectAll().where { Guilds.id eq playerGuild }.singleOrNull()?.get(Guilds.id)
            ?: return@transaction null

        val guildOwner =
            Players.selectAll().where { (Players.guildId eq guildId) and (Players.guildRank eq GuildRank.OWNER) }
                .singleOrNull()?.get(Players.playerUUID) ?: return@transaction null
        return@transaction guildOwner
    }
}

fun OfflinePlayer.isGuildOwner(): Boolean {
    return player?.getGuildOwner() == player?.uniqueId
}

fun OfflinePlayer.getGuildOwnerFromInvite(): UUID {
    return transaction(abyss.db) {
        val guilds = GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.playerUUID eq player!!.uniqueId) and (GuildJoinQueue.joinType eq GuildJoinType.INVITE) }
            .singleOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction player?.uniqueId!!

        return@transaction Players.selectAll()
            .where { (Players.guildId eq guilds) and (Players.guildRank eq GuildRank.OWNER) }
            .single()[Players.playerUUID]
    }
}

fun OfflinePlayer.getGuildLevel(): Int {
    return transaction(abyss.db) {
        val playerGuild =
            (Players.selectAll().where { Players.playerUUID eq uniqueId }.singleOrNull() ?: return@transaction 0)[Players.guildId]

        val guildLevel = Guilds.selectAll().where { Guilds.id eq playerGuild }.single()[Guilds.level]
        return@transaction guildLevel
    }
}

fun GuildName.getGuildLevel(): Int {
    return transaction(abyss.db) {
        return@transaction Guilds.selectAll().where { Guilds.name.lowerCase<String>() eq lowercase() }.singleOrNull()
            ?.get(Guilds.level) ?: return@transaction 0
    }
}

fun OfflinePlayer.getGuildBalance(): Int {
    return transaction(abyss.db) {
        val guildId = Players.selectAll().where { Players.playerUUID eq uniqueId }.first()[Players.guildId]

        val guildBalance = Guilds.selectAll().where { Guilds.id eq guildId }.single()[Guilds.balance]

        return@transaction guildBalance
    }
}

fun String.getGuildBalance(): Int {
    return transaction(abyss.db) {
        Guilds.selectAll().where { Guilds.name.lowerCase() eq lowercase() }.firstOrNull()?.get(Guilds.balance)
            ?: return@transaction 0
    }
}

fun String.setGuildBalance(newBalance: Int) {
    transaction(abyss.db) {
        Guilds.update({ Guilds.name.lowerCase() eq this@setGuildBalance.lowercase() }) {
            it[balance] = newBalance
        }
    }
}

fun OfflinePlayer.getGuildMemberCount(): Int {
    return transaction(abyss.db) {
        val playerRow = Players.selectAll().where { Players.playerUUID eq uniqueId }.first()[Players.guildId]

        val members = Players.selectAll().where { Players.guildId eq playerRow }.map { row ->
            Bukkit.getOfflinePlayer(row[Players.playerUUID])
        }

        return@transaction members.count()
    }
}

fun OfflinePlayer.hasGuildInvite(guildOwner: OfflinePlayer): Boolean {
    return transaction(abyss.db) {
        val joinQueueId = GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.playerUUID eq uniqueId) and (GuildJoinQueue.joinType eq GuildJoinType.INVITE) }
            .singleOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction false

        val ownerId = Players.selectAll().where { Players.playerUUID eq guildOwner.uniqueId }.single()[Players.guildId]

        val hasInvite = Guilds.selectAll().where { (Guilds.id eq joinQueueId) and (Guilds.id eq ownerId) }.firstOrNull()

        return@transaction hasInvite !== null
    }
}

fun OfflinePlayer.hasGuildInvites(): Boolean {
    return transaction(abyss.db) {
        return@transaction GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.joinType eq GuildJoinType.INVITE) and (GuildJoinQueue.playerUUID eq uniqueId) }
            .toList().isNotEmpty()
    }
}

fun OfflinePlayer.hasGuildRequests(): Boolean {
    return transaction(abyss.db) {
        val player = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()[Players.guildId]

        return@transaction GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.joinType eq GuildJoinType.REQUEST) and (GuildJoinQueue.guildId eq player) }
            .toList().isNotEmpty()
    }
}

fun OfflinePlayer.hasGuildRequest(): Boolean {
    return transaction(abyss.db) {
        val player = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()[Players.guildId]

        val joinQueueId = GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.guildId eq player) and (GuildJoinQueue.joinType eq GuildJoinType.REQUEST) }
            .firstOrNull()?.get(GuildJoinQueue.guildId) ?: return@transaction false

        val hasRequest = Guilds.selectAll().where { Guilds.id eq joinQueueId }.firstOrNull()

        return@transaction hasRequest !== null
    }
}

fun OfflinePlayer.getNumberOfGuildRequests(): Int {
    var requestCount = 0
    val amount = transaction(abyss.db) {
        val playerGuild =
            Players.selectAll().where { Players.playerUUID eq uniqueId }.singleOrNull()?.get(Players.guildId)
                ?: return@transaction 0

        GuildJoinQueue.selectAll()
            .where { (GuildJoinQueue.guildId eq playerGuild) and (GuildJoinQueue.joinType eq GuildJoinType.REQUEST) }
            .map { row -> row[GuildJoinQueue.guildId] }.forEach { _ ->
            requestCount++
        }
        return@transaction requestCount
    }
    return amount
}

fun GuildName.removeGuildQueueEntries(player: OfflinePlayer, guildJoinType: GuildJoinType, removeAll: Boolean = false) {
    return transaction(abyss.db) {
        val guildId = getGuildId() ?: return@transaction
        val id = GuildJoinQueue.selectAll().where { GuildJoinQueue.guildId eq guildId }.singleOrNull()
            ?.get(GuildJoinQueue.guildId) ?: return@transaction

        if (removeAll) {
            GuildJoinQueue.deleteWhere {
                (joinType eq guildJoinType) and
                        (this.guildId eq id)
            }
        } else {
            GuildJoinQueue.deleteWhere {
                (playerUUID eq player.uniqueId) and
                        (joinType eq guildJoinType) and
                        (this.guildId eq id)
            }
        }
    }
}

fun OfflinePlayer.removeGuildQueueEntries(guildJoinType: GuildJoinType, removeAll: Boolean = false) {
    return transaction(abyss.db) {
        val guildId = getGuildName()?.getGuildId() ?: return@transaction
        val id = GuildJoinQueue.selectAll().where { GuildJoinQueue.guildId eq guildId }.singleOrNull()
            ?.get(GuildJoinQueue.guildId) ?: return@transaction

        if (removeAll) {
            GuildJoinQueue.deleteWhere {
                (joinType eq guildJoinType) and
                        (this.guildId eq id)
            }
        } else {
            GuildJoinQueue.deleteWhere {
                (playerUUID eq uniqueId) and
                        (joinType eq guildJoinType) and
                        (this.guildId eq id)
            }
        }
    }
}

fun OfflinePlayer.getGuildJoinType(): GuildJoinType {
    val joinType = transaction(abyss.db) {
        val guildId = Players.selectAll().where { Players.playerUUID eq uniqueId }.single()[Players.guildId]

        val type: GuildJoinType = Guilds.selectAll().where { Guilds.id eq guildId }.single()[Guilds.joinType]

        return@transaction type
    }
    return joinType
}

fun OfflinePlayer.isCaptainOrAbove(): Boolean {
    val rank = getGuildRank() ?: false
    return rank == GuildRank.CAPTAIN || rank == GuildRank.OWNER
}
