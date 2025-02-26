package com.mineinabyss.features.guilds.data

import com.mineinabyss.features.guilds.data.entities.GuildJoinEntity
import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildJoinType
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class GuildJoinRequestsRepository(
    private val database: Database
) {
    private suspend inline fun <T> transaction(crossinline statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(db = database) { statement() }

    suspend fun getRequest(player: UUID, guildId: Int) = transaction {
        val invite = GuildJoinEntity.findById(CompositeID {
            it[GuildJoinRequestsTable.guildId] = guildId
            it[GuildJoinRequestsTable.playerUUID] = player
        })?.takeIf { it.joinType == GuildJoinType.INVITE }
        invite?.joinType
    }

    suspend fun addRequest(player: UUID, guildId: Int, type: GuildJoinType): Unit = transaction {
        GuildJoinEntity.new(CompositeID {
            it[GuildJoinRequestsTable.guildId] = guildId
            it[GuildJoinRequestsTable.playerUUID] = player
        }) {
            joinType = type
        }
    }
}
