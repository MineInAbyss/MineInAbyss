package com.mineinabyss.features.guilds.data

import com.mineinabyss.features.guilds.data.entities.GuildEntity
import com.mineinabyss.features.guilds.data.entities.GuildJoinEntity
import com.mineinabyss.features.guilds.data.entities.GuildPlayerEntity
import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import com.mineinabyss.features.guilds.data.tables.GuildsTable
import com.mineinabyss.features.guilds.ui.GuildMemberUiState
import com.mineinabyss.features.guilds.ui.GuildUiState
import com.mineinabyss.features.guilds.ui.Invite
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class GuildRepository(
    val database: Database,
) {
    private suspend inline fun <T> transaction(crossinline statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(db = database) { statement() }

    fun getGuild(guildId: Int): GuildEntity? = GuildEntity.findById(guildId)

    suspend fun guild(id: Int): GuildUiState? = transaction {
        GuildEntity.findById(id)?.toUiState()
    }

    suspend fun deleteGuild(id: Int) = transaction {
        GuildEntity.findById(id)?.delete()
    }

    suspend fun guildForPlayer(uuid: UUID): GuildUiState? = transaction {
        GuildPlayerEntity.findById(uuid)?.guild?.toUiState()
    }

    suspend fun findGuildByName(name: String) = transaction {
        GuildEntity.find { GuildsTable.name.lowerCase() eq name.lowercase() }
    }

    suspend fun getMembers(guildId: Int) = transaction {
        GuildEntity.findById(guildId)?.members ?: emptyList()
    }

    suspend fun getInvites(player: UUID): List<Invite> = transaction {
        GuildPlayerEntity.findById(player)?.joinQueue
            ?.filter { it.joinType == GuildJoinType.INVITE }
            ?.map { Invite(it.guild.toUiState()) }
            ?: emptyList()
    }

    suspend fun clearInvite(guild: Int, player: UUID) = transaction {
        GuildJoinEntity.find { (GuildJoinRequestsTable.guildId eq guild) and (GuildJoinRequestsTable.playerUUID eq player) }
            .singleOrNull()
            ?.delete()
    }

    suspend fun addMember(guild: Int, player: UUID): Boolean = transaction {
        // Remove invite if present
        clearInvite(guild, player)

        // Update player's guild
        GuildPlayerEntity.findById(player)?.guild = GuildEntity.findById(guild) ?: return@transaction false
        true
    }

    suspend fun member(uuid: UUID): GuildMemberUiState? = transaction {
        GuildPlayerEntity.findById(uuid)?.toUiState()
    }

    fun search(query: String) = GuildEntity.find { GuildsTable.name like "%$query%" }

    suspend fun updateJoinType(guildId: Int, newJoinType: GuildJoinType) = transaction {
        val guild = GuildEntity.findById(guildId) ?: return@transaction
        guild.joinType = newJoinType
    }
}
