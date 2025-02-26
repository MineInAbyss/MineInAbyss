package com.mineinabyss.features.guilds.data.tables

import org.jetbrains.exposed.dao.id.IntIdTable


object GuildMessagesTable : IntIdTable(name = "GuildMessageQueue") {
    val content = text("content")
    val playerUUID = reference("playerUUID", GuildMembersTable)
}
