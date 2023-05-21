package com.mineinabyss.relics.sickle

import com.mineinabyss.components.relics.Sickle
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.bridge.components.RightClicked
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.helpers.BlockUtil
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player

class HarvestListener : GearyListener() {
    val SourceScope.player by get<Player>()
    private val TargetScope.sickle by get<Sickle>()
    val EventScope.sickle by get<RightClicked>()

    @Handler
    fun SourceScope.doHarvest(target: TargetScope) {
        val block = player.getTargetBlockExact(3) ?: return
        val item = player.inventory.itemInMainHand

        var totalHarvested = 0
        // Harvest surroundings
        BlockUtil.NEAREST_RELATIVE_BLOCKS_FOR_RADIUS[target.sickle.radius].forEach { relativePos ->
            if (harvestPlant(BlockUtil.relative(block, relativePos), player)) {
                if (item.damage(1, player).type.isAir) {
                    player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f)
                    return@forEach
                }

                ++totalHarvested
            }
        }

        // Damage item if we harvested at least one plant
        if (totalHarvested > 0) {
            player.swingMainHand()
            player.playSound(block.location, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 2.0f)
        }
    }
}
