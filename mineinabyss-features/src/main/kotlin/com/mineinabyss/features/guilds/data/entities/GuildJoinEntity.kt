package com.mineinabyss.features.guilds.data.entities

import com.mineinabyss.features.guilds.data.tables.GuildJoinRequestsTable
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID

class GuildJoinEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<GuildJoinEntity>(GuildJoinRequestsTable)

    var player by GuildPlayerEntity referencedOn GuildJoinRequestsTable.playerUUID
    var guild by GuildEntity referencedOn GuildJoinRequestsTable.guildId
    var joinType by GuildJoinRequestsTable.joinType
}
