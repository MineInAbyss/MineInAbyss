package com.mineinabyss.features.guilds.data.entities

import com.mineinabyss.features.guilds.data.tables.GuildMessagesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildMessageEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GuildMessageEntity>(GuildMessagesTable)
    var content by GuildMessagesTable.content
    var playerUUID by GuildMessagesTable.playerUUID
}
