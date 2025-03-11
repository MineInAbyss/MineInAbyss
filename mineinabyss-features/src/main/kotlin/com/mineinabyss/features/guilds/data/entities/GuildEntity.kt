package com.mineinabyss.features.guilds.data.entities

import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildRank
import com.mineinabyss.features.guilds.data.tables.GuildsTable
import com.mineinabyss.features.guilds.data.tables.GuildMembersTable
import com.mineinabyss.features.guilds.ui.state.GuildUiState
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

class GuildEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GuildEntity>(GuildsTable)

    var name by GuildsTable.name
    var balance by GuildsTable.balance
    var level by GuildsTable.level
    var joinType by GuildsTable.joinType
    // TODO sort members by compareBy { it.player.isConnected; it.player.name; it.rank.ordinal }
    val members by GuildPlayerEntity referrersOn GuildMembersTable.guild orderBy GuildMembersTable.guildRank
    val owner: GuildPlayerEntity
        get() = GuildPlayerEntity
            .find { GuildMembersTable.guild eq id and (GuildMembersTable.guildRank eq GuildRank.OWNER) }
            .single()

    val joinQueue by GuildJoinEntity referrersOn GuildJoinRequestsTable.guildId

    fun toUiState() = GuildUiState(
        id.value,
        name,
        owner.toUiState(),
        level,
        members.count().toInt(),
        members.map { it.toUiState() },
        balance,
        joinType,
    )
}
