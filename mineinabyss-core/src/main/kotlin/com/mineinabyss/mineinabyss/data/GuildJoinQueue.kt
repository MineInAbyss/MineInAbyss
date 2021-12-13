package com.mineinabyss.mineinabyss.data

import org.jetbrains.exposed.sql.Table

object GuildJoinQueue : Table() {
    val guildId = integer("guildId") references Guilds.id
    val playerUUID = uuid("playerUUID")
    val joinType = varchar("joinType", 10)
    override val primaryKey = PrimaryKey(guildId, name = "pk_guild_id")
}

object GuildJoinType{
    val Request = "Request"
    val Invite = "Invite"
}