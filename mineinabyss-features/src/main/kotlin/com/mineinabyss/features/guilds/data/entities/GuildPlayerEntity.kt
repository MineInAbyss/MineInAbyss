package com.mineinabyss.features.guilds.data.entities

import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import com.mineinabyss.features.guilds.data.tables.GuildMessagesTable
import com.mineinabyss.features.guilds.data.tables.GuildMembersTable
import com.mineinabyss.features.guilds.extensions.toOfflinePlayer
import com.mineinabyss.features.guilds.ui.state.GuildMemberUiState
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class GuildPlayerEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<GuildPlayerEntity>(GuildMembersTable)

    var guild by GuildEntity referencedOn GuildMembersTable.guild
    var guildRank by GuildMembersTable.guildRank
    val uuid by GuildMembersTable.id
    val messages by GuildMessageEntity referrersOn GuildMessagesTable.playerUUID
    val joinQueue by GuildJoinEntity referrersOn GuildJoinRequestsTable.playerUUID

    fun toUiState(): GuildMemberUiState {
        val uuid = uuid.value
        val player = uuid.toOfflinePlayer()
        return GuildMemberUiState(
            player.name ?: "Unknown Player",
            uuid,
            guildRank,
            guild.id.value
        )
    }
}
