package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.v1.core.Table

object Players : Table() {
    val playerUUID = uuid("playerUUID").uniqueIndex()
    val guildId = integer("guildId") references Guilds.id
    val guildRank = enumeration("guildRank", GuildRank::class)
    override val primaryKey = PrimaryKey(playerUUID, name = "pk_players_uuid")
}
