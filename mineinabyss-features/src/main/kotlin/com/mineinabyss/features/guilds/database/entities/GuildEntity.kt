package com.mineinabyss.features.guilds.database.entities

import com.mineinabyss.features.guilds.database.Guilds
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<GuildEntity>(Guilds)

    var name by Guilds.name
    var balance by Guilds.balance
    var level by Guilds.level
    var joinType by Guilds.joinType
}
