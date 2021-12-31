package com.mineinabyss.mineinabyss.data

import org.jetbrains.exposed.dao.id.IntIdTable


object MessageQueue : IntIdTable() {
    val content = text("content")
    val playerUUID = uuid("playerUUID")
}