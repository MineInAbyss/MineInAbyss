package com.mineinabyss.features.guilds.data

import com.mineinabyss.features.guilds.data.tables.GuildMembersTable
import com.mineinabyss.features.guilds.data.entities.GuildEntity
import com.mineinabyss.features.guilds.data.entities.GuildMessageEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class GuildMessagesRepository(
    private val database: Database
) {
    private suspend inline fun <T> transaction(crossinline statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(db = database) { statement() }

    suspend fun messageAllGuildMembers(guildId: Int, message: String, exclude: Set<UUID> = setOf()) = transaction {
        GuildEntity.findById(guildId)?.members?.forEach {
            val uuid = it.uuid.value
            if(uuid !in exclude) messagePlayer(uuid, message)
        }
    }
    suspend fun messagePlayer(player: UUID, message: String) = transaction {
        val id = EntityID(player, GuildMembersTable)
        GuildMessageEntity.new {
            content = message
            playerUUID = id
        }
    }
}
