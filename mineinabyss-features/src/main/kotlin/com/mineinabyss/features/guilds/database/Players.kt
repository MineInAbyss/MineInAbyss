package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.dao.id.UUIDTable

object Players : UUIDTable(columnName = "playerUUID") {
    val guild = reference("guildId", Guilds)
    val guildRank = enumeration("guildRank", GuildRank::class)
}
