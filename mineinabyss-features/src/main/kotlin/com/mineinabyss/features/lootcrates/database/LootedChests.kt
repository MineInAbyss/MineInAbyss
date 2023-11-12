package com.mineinabyss.features.lootcrates.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object LootedChests: IntIdTable() {
    val playerUUID = uuid("playerUUID")
    val dateLooted = date("dateLooted")
    val chestId = uuid("chestId")
    val lootType = varchar("lootType", 50)
}
