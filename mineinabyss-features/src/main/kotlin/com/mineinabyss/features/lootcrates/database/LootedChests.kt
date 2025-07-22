package com.mineinabyss.features.lootcrates.database

import org.bukkit.Location
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.date

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
