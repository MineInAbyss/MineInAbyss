package com.derongan.minecraft.mineinabyss.ecs

import com.derongan.minecraft.mineinabyss.harvestPlant
import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.idofront.items.damage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import java.util.*


@Serializable
@SerialName("mineinabyss:harvest")
class HarvestAction : GearyAction() {
    private val GearyEntity.sickle by get<Sickle>()

    override fun GearyEntity.run(): Boolean {
        val player = parent?.get<Player>() ?: return false

        val block = player.getTargetBlock(3) ?: return false
        val item = player.inventory.itemInMainHand

        var totalHarvested = 0
        // Harvest surroundings
        for (relativePos in BlockUtil.NEAREST_RELATIVE_BLOCKS_FOR_RADIUS[sickle.radius]) {
            val block = BlockUtil.relative(block, relativePos) ?: continue
            if (harvestPlant(block, player)) {
                ++totalHarvested
            }
        }

        // Damage item if we harvested at least one plant
        if (totalHarvested > 0) {
            item.damage = item.damage?.plus((1 + (0.25 * totalHarvested).toInt()))
            player.swingMainHand()
            block.world
                .playSound(block.location, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 2.0f)
        }

        return true
    }
}


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
        Collections.sort(ret, BlockVectorRadiusComparator)
        return ret
    }


    fun relative(block: Block, relative: Vector): Block? {
        return block.getRelative(relative.blockX, relative.blockY, relative.blockZ)
    }
}

object BlockVectorRadiusComparator : Comparator<BlockVector> {
    override fun compare(a: BlockVector, b: BlockVector): Int {
        return (a.blockX * a.blockX + a.blockY * a.blockY + a.blockZ * a.blockZ
                - (b.blockX * b.blockX + b.blockY * b.blockY + b.blockZ * b.blockZ))
    }
}