package com.mineinabyss.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.displaylocker.lockedStand
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class DisplayLockerListener: Listener {
    @EventHandler
    fun HangingPlaceEvent.onPlaceItemFrame() {
        val frame = entity as? ItemFrame ?: return
        val player = player ?: return
        frame.toGeary().setPersisting(LockDisplayItem(player.uniqueId, false, mutableSetOf(player.uniqueId)))
        player.playerData.recentRightclickedEntity = frame
        player.error("Use </mia lock toggle> to protect this Armor Stand")
    }

    @EventHandler
    fun PlayerInteractEvent.onPlaceArmorStand() { // pretty much the same code as before
        if (player.inventory.itemInMainHand.type != Material.ARMOR_STAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (blockFace == BlockFace.DOWN) return
        isCancelled = true

        val initialLocation = player.getLastTwoTargetBlocks(null, 6)
        val newLocation =
            if (blockFace == BlockFace.UP) initialLocation.last()?.location?.toCenterLocation()?.apply { y += 0.5 } ?: return
            else initialLocation.first()?.location?.toCenterLocation()?.apply { y -= 0.5 } ?: return

        newLocation.spawn<ArmorStand>()?.apply {
            setRotation(player.location.yaw - 180, 0.0F)
            toGearyOrNull()?.setPersisting(LockDisplayItem(player.uniqueId, false, mutableSetOf(player.uniqueId)))
            this.toGearyOrNull()?.encodeComponentsTo(this)
            player.playerData.recentRightclickedEntity = this
        } ?: return

        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        player.playSound(newLocation, Sound.ENTITY_ARMOR_STAND_PLACE, 1f, 1f)
        player.error("Use </mia lock toggle> to protect this Armor Stand")
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractLockedArmorStand() {
        val armorStand = rightClicked.lockedStand ?: return

        if (armorStand.owner == player.uniqueId && player.inventory.itemInMainHand.type == Material.AIR)
            player.playerData.recentRightclickedEntity = rightClicked

        if (!armorStand.lockState) return
        if (!armorStand.isAllowed(player.uniqueId) && !player.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            player.error("You do not have access to interact with this display item!")
            isCancelled = true
            return
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingArmorStand() {
        val armorStand = entity.lockedStand ?: return

        if (!armorStand.lockState) return

        val attacker: Player = when (damager) {
            is Projectile -> (damager as Projectile).shooter as? Player ?: return
            is Player -> (damager as Player)
            else -> return
        }

        if (!armorStand.isAllowed(attacker.uniqueId) && !attacker.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            attacker.error("You do not have access to interact with this ${entity.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerFishEvent.onFishArmorstand() {
        if (state != PlayerFishEvent.State.CAUGHT_ENTITY) return

        val armorStandBukkit = caught as? ArmorStand ?: return
        val armorStandGeary = armorStandBukkit.lockedStand ?: return

        if (!armorStandGeary.lockState) return

        if (!armorStandGeary.isAllowed(player.uniqueId) && !player.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            player.error("You do not have access to this display item")
            hook.remove()
            isCancelled = true
        }
    }

    /**
     * This function is used to prevent pistons from extending armor stands,
     * to prevent pistons from pushing blocks into armor stands,
     * and to prevent pistons from pushing armor stands using honey blocks below them.
     */
    private fun onPistonExtendRetract(block: Block, direction: BlockFace, blocks: List<Block>): Boolean {
        val blocksThePistonChanges =
            (blocks + block).map { it.location.add(direction.direction).block } + blocks.filter { it.type == Material.HONEY_BLOCK }
                .map { it.location.add(BlockFace.UP.direction).block }

        for (blockinblocks in blocksThePistonChanges) {
            // Get all armor stands in the blocks the piston changes
            val armorStands = blockinblocks.location.getNearbyEntitiesByType(ArmorStand::class.java, 1.0)
            for (armorStand in armorStands) {
                // If any armorstand is locked, cancel the event
                val gearyArmorStand = armorStand.lockedStand ?: return false
                if (!gearyArmorStand.lockState) continue

                return true
            }
        }
        return false
    }

    @EventHandler
    fun BlockPistonExtendEvent.onPistonExtend() {
        if (onPistonExtendRetract(block, direction, blocks)) {
            isCancelled = true
        }
    }

    @EventHandler
    fun BlockPistonRetractEvent.onPistonRetract() {
        if (onPistonExtendRetract(block, direction.oppositeFace, blocks)) {
            isCancelled = true
        }
    }
}