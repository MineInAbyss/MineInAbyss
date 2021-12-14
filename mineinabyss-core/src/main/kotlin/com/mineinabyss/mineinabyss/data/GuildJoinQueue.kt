package com.mineinabyss.mineinabyss.data

import org.jetbrains.exposed.sql.Table

object GuildJoinQueue : Table() {
    val playerUUID = uuid("playerUUID")
    val guildId = integer("guildId") references Guilds.id
    val joinType = varchar("joinType", 10)
}

object GuildJoinType{
    val Request = "Request"
    val Invite = "Invite"
}