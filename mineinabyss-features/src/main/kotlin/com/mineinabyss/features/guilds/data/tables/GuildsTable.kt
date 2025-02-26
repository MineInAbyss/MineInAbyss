package com.mineinabyss.features.guilds.data.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object GuildsTable : IntIdTable(name = "Guilds") {
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
