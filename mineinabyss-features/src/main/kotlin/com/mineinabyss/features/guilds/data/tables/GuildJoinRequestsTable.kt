package com.mineinabyss.features.guilds.data.tables

import org.jetbrains.exposed.dao.id.CompositeIdTable

object GuildJoinRequestsTable : CompositeIdTable(name = "GuildJoinQueue") {
    val playerUUID = reference("playerUUID", GuildMembersTable)
    val guildId = reference("guildId", GuildsTable)
    val joinType = enumeration("joinType", GuildJoinType::class)
    override val primaryKey = PrimaryKey(playerUUID, guildId)
}

