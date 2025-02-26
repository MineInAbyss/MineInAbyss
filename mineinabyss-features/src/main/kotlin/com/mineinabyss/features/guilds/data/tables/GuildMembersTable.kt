package com.mineinabyss.features.guilds.data.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object GuildMembersTable : UUIDTable(name = "Players", columnName = "playerUUID") {
    val guild = reference("guildId", GuildsTable)
    val guildRank = enumeration("guildRank", GuildRank::class)
}
