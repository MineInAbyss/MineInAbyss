package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.dao.id.IntIdTable

object Guilds : IntIdTable() {
    val name = varchar("guildName", 30)
    val balance = integer("guildBalance")
    val level = integer("guildLevel")
    val joinType = enumeration("joinType", GuildJoinType::class)
}

enum class GuildRank {
    OWNER, CAPTAIN, STEWARD, MEMBER
}

enum class GuildJoinType {
    ANY, REQUEST, INVITE
}
