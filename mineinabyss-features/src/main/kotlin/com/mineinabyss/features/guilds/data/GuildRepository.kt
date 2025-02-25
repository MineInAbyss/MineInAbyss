package com.mineinabyss.features.guilds.data

import com.mineinabyss.features.guilds.database.Guilds
import com.mineinabyss.features.guilds.database.entities.GuildEntity
import com.mineinabyss.features.guilds.database.entities.GuildPlayerEntity
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.menus.GuildMemberUiState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class GuildRepository(
    val database: Database,
) {
    suspend inline fun <T> transaction(crossinline statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(db = database) { statement() }

    fun getGuild(guildId: Int): GuildEntity? = GuildEntity.findById(guildId)

    suspend fun guildInfo(id: Int) = transaction {
        val guildName = Guilds.selectAll()
            .where { Guilds.id eq id }
            .singleOrNull()

            ?: return@transaction null
        guildName
    }

    suspend fun member(uuid: UUID): GuildMemberUiState? {
        val entity = transaction {
            GuildPlayerEntity.findById(uuid)
        } ?: return null
        val uuid = entity.uuid.value
        val player = uuid.toOfflinePlayer()
        return GuildMemberUiState(
            player.name ?: "Unknown Player",
            uuid,
            entity.guildRank,
        )
    }

    fun search(query: String) = GuildEntity.find { Guilds.name like "%$query%" }

    fun updateName(guild: GuildEntity, newName: String) {
        require(GuildEntity.find { Guilds.name eq guild.name }.empty()) { "Guild with this name already exists" }
        guild.name = newName
    }
}
