package com.mineinabyss.helpers

import org.bukkit.block.Block
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector

object BlockUtil {
    private const val NEAREST_RELATIVE_BLOCKS_FOR_RADIUS_MAX = 6

    val NEAREST_RELATIVE_BLOCKS_FOR_RADIUS: MutableList<List<BlockVector>> = ArrayList()

    init {
        for (i in 1..NEAREST_RELATIVE_BLOCKS_FOR_RADIUS_MAX) {
            NEAREST_RELATIVE_BLOCKS_FOR_RADIUS.add(nearestBlocksForRadius(i))
        }
    }

    private fun nearestBlocksForRadius(radius: Int): List<BlockVector> {
        val ret = ArrayList<BlockVector>()

        // Use square bounding box
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                // Only circular area
                if (x * x + z * z > radius * radius + 0.5) {
                    continue
                }
                ret.add(BlockVector(x, 0, z))
            }
        }
        ret.sortWith { a, b ->
            (a.blockX * a.blockX + a.blockY * a.blockY + a.blockZ * a.blockZ
                    - (b.blockX * b.blockX + b.blockY * b.blockY + b.blockZ * b.blockZ))
        }
        return ret
    }


    fun relative(block: Block, relative: Vector): Block {
        return block.getRelative(relative.blockX, relative.blockY, relative.blockZ)
    }
}
