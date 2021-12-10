package com.mineinabyss.mineinabyss.data

import org.jetbrains.exposed.sql.Table

object Players : Table() {
    val playerUUID = uuid("playerUUID").uniqueIndex()
    val guildId = integer("guildId") references Guilds.id
    val guildRank = integer("guildRank")
    override val primaryKey = PrimaryKey(playerUUID, name = "pk_players_uuid")
}