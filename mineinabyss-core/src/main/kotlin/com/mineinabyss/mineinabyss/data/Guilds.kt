package com.mineinabyss.mineinabyss.data

import org.jetbrains.exposed.sql.Table

object Guilds : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("guildName", 30)
    val balance = integer("guildBalance")
    val level = integer("guildLevel")
    override val primaryKey = PrimaryKey(id, name = "pk_guilds_id")
}

object GuildRanks{
    val Owner = 4
    val Captain = 3
    val Steward = 2
    val Member = 1
}

object GuildLevels{
    val Level4 = 4
    val Level3 = 3
    val Level2 = 2
    val Level1 = 1
}