package com.mineinabyss.guilds.database

import org.jetbrains.exposed.sql.Table

object Guilds : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("guildName", 30)
    val balance = integer("guildBalance")
    val level = integer("guildLevel")
    val joinType = enumeration("joinType", GuildJoinType::class)
    override val primaryKey = PrimaryKey(id, name = "pk_guilds_id")
}

enum class GuildRanks {
    OWNER, CAPTAIN, STEWARD, MEMBER
}

enum class GuildJoinType {
    Any, Request, Invite
}
