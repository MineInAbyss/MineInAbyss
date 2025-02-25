package com.mineinabyss.features.guilds.database.entities

import com.mineinabyss.features.guilds.database.Players
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class GuildPlayerEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<GuildPlayerEntity>(Players)

    var guild by GuildEntity referencedOn Players.guild
    var guildRank by Players.guildRank
    val uuid by Players.id
}
