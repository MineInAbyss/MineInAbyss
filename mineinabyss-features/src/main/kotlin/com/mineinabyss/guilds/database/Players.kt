package com.mineinabyss.guilds.database

import org.jetbrains.exposed.sql.Table

object Players : Table() {
    val playerUUID = uuid("playerUUID").uniqueIndex()
    val guildId = integer("guildId") references Guilds.id
    val guildRank = enumeration("guildRank", GuildRanks::class)
    override val primaryKey = PrimaryKey(playerUUID, name = "pk_players_uuid")
}