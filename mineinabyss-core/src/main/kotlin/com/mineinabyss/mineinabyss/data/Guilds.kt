package com.mineinabyss.mineinabyss.data

import org.jetbrains.exposed.sql.Table

object Guilds : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("guildName", 30)
    val balance = integer("guildBalance")
    val level = integer("guildLevel")
    val joinType = varchar("joinType", 10)
    override val primaryKey = PrimaryKey(id, name = "pk_guilds_id")
}

object GuildRanks{
    val Owner = "Owner"
    val Captain = "Captain"
    val Steward = "Steward"
    val Member = "Member"
}
