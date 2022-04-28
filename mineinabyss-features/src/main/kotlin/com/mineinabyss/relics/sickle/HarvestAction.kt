package com.mineinabyss.relics.sickle

import com.mineinabyss.components.relics.Sickle
import com.mineinabyss.geary.annotations.AutoScan
import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.events.bridge.components.RightClicked
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.get
import com.mineinabyss.helpers.BlockUtil
import com.mineinabyss.idofront.items.damage
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player

@AutoScan
class HarvestListener : GearyListener() {
    val SourceScope.player by get<Player>()
    val TargetScope.sickle by get<Sickle>()
    val EventScope.sickle by get<RightClicked>()

    @Handler
    fun SourceScope.doHarvest(target: TargetScope) {
        val block = player.getTargetBlock(3) ?: return
        val item = player.inventory.itemInMainHand

        var totalHarvested = 0
        // Harvest surroundings
        for (relativePos in BlockUtil.NEAREST_RELATIVE_BLOCKS_FOR_RADIUS[target.sickle.radius]) {
            val block = BlockUtil.relative(block, relativePos) ?: continue
            if (harvestPlant(block, player)) {
                item.editItemMeta {
                    damage += 1
                }
                if (item.itemMeta.damage >= item.type.maxDurability) {
                    item.subtract()
                    player.world
                        .playSound(player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f)
                    break // stop loop
                }
                ++totalHarvested
            }
        }

        // Damage item if we harvested at least one plant
        if (totalHarvested > 0) {
            player.swingMainHand()
            block.world
                .playSound(block.location, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 2.0f)
        }
    }
}
