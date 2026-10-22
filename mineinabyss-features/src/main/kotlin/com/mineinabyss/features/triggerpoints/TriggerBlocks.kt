package com.mineinabyss.features.triggerpoints

import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.ChunkPos
import com.mineinabyss.features.helpers.chunkPos
import com.mineinabyss.features.helpers.pos
import com.mineinabyss.geary.prefabs.PrefabKey
import com.nexomc.nexo.api.NexoBlocks
import com.nexomc.nexo.utils.drops.Drop
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

// Flat x, y, z triples of the trigger blocks placed in a chunk, so blocks of moved or removed points can be taken away
private val PLACED_KEY = NamespacedKey("mineinabyss", "trigger_points/blocks")

// Trigger blocks placed before trigger points existed were recorded here, it is still read so those blocks get removed or taken over
private val LEGACY_PLACED_KEY = NamespacedKey("mineinabyss", "warp/trigger_blocks")

class TriggerBlocks(config: TriggerPointsConfig) : Listener {
    private data class BlockPos(val world: UUID, val x: Int, val y: Int, val z: Int)
    private class Placement(val location: Location, val point: PrefabKey)

    private val nexoId = nexoId(config.triggerBlock)
    private var byBlock = mapOf<BlockPos, Placement>()
    private var byChunk = mapOf<ChunkPos, List<Placement>>()
    private val warnedBlocked = mutableSetOf<BlockPos>()

    fun rebuild(points: Map<PrefabKey, List<Location>>) {
        val placements = points.flatMap { (key, blocks) -> blocks.map { Placement(it, key) } }
        byBlock = placements.associateBy { it.location.blockPos() }
        byChunk = placements.groupBy { it.location.chunkPos }
        warnedBlocked.clear()
    }

    // A trigger configured inside a partial floor sits in the block above it, where the player standing on it may also be
    fun pointAt(block: Block): PrefabKey? = block.triggerPoint()
        ?: block.getRelative(BlockFace.UP).takeIf { block.isPartialFloor() }?.triggerPoint()
        ?: block.getRelative(BlockFace.DOWN).takeIf { it.isPartialFloor() }?.triggerPoint()

    private fun Block.triggerPoint() = byBlock[location.blockPos()]?.point

    fun isRegistered() = NexoBlocks.customBlockMechanic(nexoId) != null

    fun placeInLoadedChunks() = Bukkit.getWorlds().flatMap { world -> world.loadedChunks.flatMap(::placeIn) }.let(::warnBlocked)

    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() = warnBlocked(placeIn(chunk))

    // Returns the placements whose trigger could not be placed
    private fun placeIn(chunk: Chunk): List<Placement> {
        if (!isRegistered()) return emptyList()
        val data = chunk.persistentDataContainer
        val expected = byChunk[chunk.pos].orEmpty()
        val stored = data.get(PLACED_KEY, PersistentDataType.INTEGER_ARRAY)?.toList()?.chunked(3).orEmpty()
        val legacy = data.get(LEGACY_PLACED_KEY, PersistentDataType.INTEGER_ARRAY)?.toList()?.chunked(3).orEmpty()
        val recorded = stored + legacy
        if (expected.isEmpty() && recorded.isEmpty()) return emptyList()

        val blocked = mutableListOf<Placement>()
        val placed = expected.mapNotNull { placement ->
            val block = placement.location.block
            val above = block.getRelative(BlockFace.UP)
            when {
                isTrigger(block) -> block
                block.isReplaceable -> block.also { NexoBlocks.place(nexoId, it.location) }
                block.isPartialFloor() && isTrigger(above) -> above
                block.isPartialFloor() && above.isReplaceable -> above.also { NexoBlocks.place(nexoId, it.location) }
                else -> null.also { blocked += placement }
            }
        }

        val positions = placed.map { listOf(it.x, it.y, it.z) }
        val wanted = positions.toSet()
        recorded.filter { it.size == 3 && it !in wanted }.forEach { (x, y, z) ->
            val block = chunk.world.getBlockAt(x, y, z)
            if (isTrigger(block)) NexoBlocks.remove(block.location, overrideDrop = Drop.emptyDrop())
        }
        if (legacy.isNotEmpty()) data.remove(LEGACY_PLACED_KEY)
        if (positions == stored && legacy.isEmpty()) return blocked

        if (positions.isEmpty()) data.remove(PLACED_KEY)
        else data.set(PLACED_KEY, PersistentDataType.INTEGER_ARRAY, positions.flatten().toIntArray())

        return blocked
    }

    private fun warnBlocked(blocked: List<Placement>) = blocked
        .filter { warnedBlocked.add(it.location.blockPos()) }
        .groupBy { it.point }
        .forEach { (point, placements) ->
            val where = placements.joinToString { "${it.location.block.type} at ${it.location.blockX},${it.location.blockY},${it.location.blockZ}" }
            abyss.logger.w("Trigger point $point has ${placements.size} trigger ${if (placements.size == 1) "block" else "blocks"} inside other blocks: $where")
        }

    private fun isTrigger(block: Block) = NexoBlocks.customBlockMechanic(block)?.itemID == nexoId

    // Players stand on top of these, so their feet are inside the block rather than above it.
    // Bottom stairs are listed apart since their collision shape reaches the full block height
    private fun Block.isPartialFloor(): Boolean = when (val data = blockData) {
        is Slab -> data.type == Slab.Type.BOTTOM
        is Stairs -> data.half == Bisected.Half.BOTTOM
        else -> collisionShape.boundingBoxes.let { boxes -> boxes.isNotEmpty() && boxes.maxOf { it.maxY } < 1.0 }
    }

    private fun Location.blockPos() = BlockPos(world.uid, blockX, blockY, blockZ)

    // Mirrors the Nexo item id geary-nexo registers prefabs under, which it keeps internal
    private fun nexoId(prefab: PrefabKey) = "${prefab.namespace}_${prefab.key}"
}
