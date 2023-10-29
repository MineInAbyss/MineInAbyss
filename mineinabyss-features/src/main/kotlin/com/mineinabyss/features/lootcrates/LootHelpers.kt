package com.mineinabyss.features.lootcrates

import org.bukkit.Location
import java.util.*

fun Location.toLootChestUUID(): UUID {
    val uuper: ULong = world.name.hashCode().toULong() xor (blockY.toULong() shl 32)
    val lower: ULong = blockX.toULong() xor (blockZ.toULong() shl 32)
    return UUID(uuper.toLong(), lower.toLong())
}
