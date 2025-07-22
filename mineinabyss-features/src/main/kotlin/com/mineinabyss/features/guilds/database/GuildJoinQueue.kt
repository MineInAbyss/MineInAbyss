package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.v1.core.Table

object GuildJoinQueue : Table() {
    val playerUUID = uuid("playerUUID")
    val guildId = integer("guildId") references Guilds.id
    val joinType = enumeration("joinType", GuildJoinType::class)
}

