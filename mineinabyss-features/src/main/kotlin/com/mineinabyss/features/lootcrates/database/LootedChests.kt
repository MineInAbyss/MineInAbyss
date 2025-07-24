package com.mineinabyss.features.lootcrates.database

import org.bukkit.Location
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.date

object LootedChests : IntIdTable() {
    val playerUUID = uuid("playerUUID")
    val dateLooted = date("dateLooted")
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val world = varchar("world", 50)
    val lootType = varchar("lootType", 50)

    fun locationEq(location: Location): Op<Boolean> {
        val currX = location.blockX
        val currY = location.blockY
        val currZ = location.blockZ
        val currWorld = location.world
        return (x eq currX) and (y eq currY) and (z eq currZ) and (world eq currWorld.name)
    }
}
