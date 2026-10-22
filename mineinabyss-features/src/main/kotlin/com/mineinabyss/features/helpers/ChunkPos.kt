package com.mineinabyss.features.helpers

import org.bukkit.Chunk
import org.bukkit.Location
import java.util.*

data class ChunkPos(val world: UUID, val key: Long)

val Location.chunkPos get() = ChunkPos(world.uid, Chunk.getChunkKey(this))

val Chunk.pos get() = ChunkPos(world.uid, chunkKey)
