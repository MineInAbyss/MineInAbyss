package com.derongan.minecraft.mineinabyss

import com.mineinabyss.idofront.plugin.getPlugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.random.Random

/**
 * A reference to the MineInAbyss plugin
 */
val mineInAbyss: MineInAbyss by lazy { getPlugin() }

fun harvestPlant(block: Block, player: Player): Boolean {
    val handItem = player.inventory.itemInMainHand

    @Suppress("RemoveExplicitTypeArguments")
    val blockList = mapOf<Material, Set<ItemDrop>>(
        Material.WHEAT to setOf(ItemDrop(Material.WHEAT, 1..3)),
        Material.CARROTS to setOf(ItemDrop(Material.CARROT, 1..3)),
        Material.POTATOES to setOf(ItemDrop(Material.POTATO, 1..3)),
        Material.BEETROOTS to setOf(ItemDrop(Material.BEETROOT, 1..3)),
        Material.NETHER_WART to setOf(ItemDrop(Material.NETHER_WART, 1..3)),
        Material.COCOA to setOf(ItemDrop(Material.COCOA_BEANS, 1..3)),
        Material.MELON to setOf(ItemDrop(Material.MELON_SLICE, 1..3)),
        Material.PUMPKIN to setOf(ItemDrop(Material.PUMPKIN, 1..1, false))
    )

    val drops: Set<ItemDrop> = blockList[block.type] ?: return false

    if (block.blockData is Ageable) {
        val aging: Ageable = block.blockData as Ageable

        if (aging.age != aging.maximumAge) return false

        val breakCrop = BlockBreakEvent(block, player)
        Bukkit.getPluginManager().callEvent(breakCrop)
        if (breakCrop.isCancelled) return false

        aging.age = 0
        block.blockData = aging
    } else {
        val breakBlock = BlockBreakEvent(block, player)
        Bukkit.getPluginManager().callEvent(breakBlock)
        if (breakBlock.isCancelled) return false

        block.type = Material.AIR // Break block
    }

    fun applyFortune(count: Int): Int {
        if (handItem.type != Material.WOODEN_HOE &&
            handItem.type != Material.STONE_HOE &&
            handItem.type != Material.IRON_HOE &&
            handItem.type != Material.GOLDEN_HOE &&
            handItem.type != Material.DIAMOND_HOE &&
            handItem.type != Material.NETHERITE_HOE
        ) return count
        val level = handItem.itemMeta?.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS) ?: return count

        // Do we have bonus drops?
        return if (Random.nextDouble() > 2 / (level + 2)) {
            // Yes, how many extra drops?
            count + (2..level + 1).random()
        } else {
            count
        }
    }

    for (drop in drops) {
        dropItems(
            block.location,
            ItemStack(
                drop.material,
                if (drop.applyFortune) applyFortune(drop.dropAmount.random()) else drop.dropAmount.random()
            )
        )
    }

    return true
}

private fun dropItems(loc: Location, drop: ItemStack) {
    loc.world.dropItem(loc.add(Vector.getRandom().subtract(Vector(.5, .5, .5)).multiply(0.5)), drop).velocity =
        Vector.getRandom().add(Vector(-.5, +.5, -.5)).normalize().multiply(.15)
}

data class ItemDrop(
    val material: Material,
    val dropAmount: IntRange,
    val applyFortune: Boolean = true
)