package com.mineinabyss.features.guilds.data

import com.mineinabyss.features.guilds.database.entities.GuildEntity
import com.mineinabyss.features.guilds.database.Guilds
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class GuildRepository(
    val database: Database,
) {
    suspend inline fun <T> transaction(crossinline statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(db = database) { statement() }

    suspend fun guildInfo(id: Int) = transaction {
        val guildName = Guilds.selectAll()
            .where { Guilds.id eq id }
            .singleOrNull()

            ?: return@transaction null
        guildName
    }

    fun search(query: String) = GuildEntity.find { Guilds.name like "%$query%" }

    fun updateName(guild: GuildEntity, newName: String) {
        require(GuildEntity.find { Guilds.name eq guild.name }.empty()) { "Guild with this name already exists" }
        guild.name = newName
    }
}
