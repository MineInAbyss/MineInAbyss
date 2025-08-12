package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.v1.core.Table

object Guilds : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("guildName", 30)
    val balance = integer("guildBalance")
    val level = integer("guildLevel")
    val joinType = enumeration("joinType", GuildJoinType::class)
    override val primaryKey = PrimaryKey(id, name = "pk_guilds_id")
}

enum class GuildRank {
    OWNER, CAPTAIN, STEWARD, MEMBER
}

enum class GuildJoinType {
    ANY, REQUEST, INVITE
}
