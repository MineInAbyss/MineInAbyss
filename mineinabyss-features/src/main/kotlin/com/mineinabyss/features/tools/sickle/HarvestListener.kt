package com.mineinabyss.features.tools.sickle

import com.mineinabyss.components.tools.Sickle
import com.mineinabyss.features.helpers.BlockUtil
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

class HarvestListener : GearyListener() {
    val Pointers.player by get<Player>().on(target)
    private val Pointers.sickle by get<Sickle>().on(source)

    override fun Pointers.handle() {
        val block = player.getTargetBlockExact(5) ?: return
        val item = player.inventory.itemInMainHand

        var totalHarvested = 0
        // Harvest surroundings
        BlockUtil.NEAREST_RELATIVE_BLOCKS_FOR_RADIUS[sickle.radius].forEach { relativePos ->
            if (harvestPlant(BlockUtil.relative(block, relativePos), player)) {
                if (!item.damage(1, player).isEmpty)
                    ++totalHarvested
            }
            val range = sickle.radius.let { -it..it }
            for (x in range) for (y in range) for (z in range) {
                val leaf = block.getRelative(x, y, z).takeIf { it.type in Tag.LEAVES.values } ?: continue
                if (!BlockBreakEvent(leaf, player).callEvent()) continue
                if (!item.damage(1, player).isEmpty) {
                    leaf.breakNaturally(item, true)
                    ++totalHarvested
                }
            }
        }

        // Damage item if we harvested at least one plant
        if (totalHarvested > 0) {
            player.swingMainHand()
            player.playSound(block.location, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 2.0f)
        }
    }
}
