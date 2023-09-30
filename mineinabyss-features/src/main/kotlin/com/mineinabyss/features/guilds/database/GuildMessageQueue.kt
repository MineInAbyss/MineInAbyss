package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.dao.id.IntIdTable


object GuildMessageQueue : IntIdTable() {
    val content = text("content")
    val playerUUID = uuid("playerUUID")
}
