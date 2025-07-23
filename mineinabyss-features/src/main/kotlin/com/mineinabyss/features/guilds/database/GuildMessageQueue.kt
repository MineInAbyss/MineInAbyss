package com.mineinabyss.features.guilds.database

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable


object GuildMessageQueue : IntIdTable() {
    val content = text("content")
    val playerUUID = uuid("playerUUID")
}
