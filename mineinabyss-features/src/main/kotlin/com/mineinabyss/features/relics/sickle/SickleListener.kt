package com.mineinabyss.features.relics.sickle

import com.destroystokyo.paper.MaterialTags
import com.mineinabyss.features.helpers.ItemDrop
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class SickleListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerInteractEvent.onPlayerHarvest() {
        val block = clickedBlock ?: return

        if (hand != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK) return
        if (harvestPlant(block, player)) {
            player.swingMainHand()
            block.world.playSound(block.location, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 2.0f)
        }
    }
}

fun harvestPlant(block: Block, player: Player): Boolean {
    val handItem = player.inventory.itemInMainHand
    val data = block.blockData

    if (handItem.type == Material.SHEARS) return false
    if (handItem.type == block.type) return false

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

    if (data is Ageable) {
        if (data.age != data.maximumAge) return false

        val breakCrop = BlockBreakEvent(block, player)
        Bukkit.getPluginManager().callEvent(breakCrop)
        if (breakCrop.isCancelled) return false

        data.age = 0
        block.blockData = data
    } else {
        val breakBlock = BlockBreakEvent(block, player)
        Bukkit.getPluginManager().callEvent(breakBlock)
        if (breakBlock.isCancelled) return false

        block.type = Material.AIR // Break block
    }

    fun applyFortune(count: Int): Int {
        if (!MaterialTags.HOES.isTagged(handItem)) return count
        val level = handItem.itemMeta?.getEnchantLevel(Enchantment.LOOT_BONUS_BLOCKS) ?: return count

        // Do we have bonus drops?
        return if (Random.nextDouble() > 2 / (level + 2)) {
            // Yes, how many extra drops?
            count + (2..level + 1).random()
        } else count
    }

    drops.forEach { drop ->
        block.world.dropItemNaturally(
            block.location, ItemStack(
                drop.material,
                if (drop.applyFortune) applyFortune(drop.dropAmount.random()) else drop.dropAmount.random()
            )
        )
    }

    return true
}
