package com.mineinabyss.features.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.displaylocker.lockedDisplay
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.datastore.encodeComponentsTo
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.spawning.spawn
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
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
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class DisplayLockerListener : Listener {
    @EventHandler
    fun HangingPlaceEvent.onPlaceItemFrame() {
        val (frame, player) = (entity as? ItemFrame ?: return) to (player ?: return)
        val lockState = player.playerData.defaultDisplayLockState
        frame.toGeary()
            .getOrSetPersisting { LockDisplayItem(player.uniqueId, lockState, mutableSetOf(player.uniqueId)) }
        frame.toGeary().encodeComponentsTo(frame)
        player.playerData.recentInteractEntity = frame.uniqueId
        when (lockState) {
            true -> player.success("This Item Frame is now protected!")
            false -> player.error("Use <b>/mia lock toggle</b> to protect this Item Frame.")
        }
    }

    @EventHandler
    fun PlayerInteractEvent.onPlaceArmorStand() { // pretty much the same code as before
        if (player.inventory.itemInMainHand.type != Material.ARMOR_STAND) return
        if (hand != EquipmentSlot.HAND || blockFace == BlockFace.DOWN || action != Action.RIGHT_CLICK_BLOCK) return
        val lockState = player.playerData.defaultDisplayLockState
        isCancelled = true

        val initialLocation = player.getLastTwoTargetBlocks(null, 6)
        val newLocation = initialLocation.last()?.location?.toCenterLocation()?.apply {
            when (blockFace) {
                BlockFace.UP -> y += 0.5
                else -> y -= 0.5
            }
        } ?: return

        newLocation.spawn<ArmorStand>()?.apply {
            setRotation(player.location.yaw - 180, 0.0F)
            toGeary().getOrSetPersisting { LockDisplayItem(player.uniqueId, lockState, mutableSetOf(player.uniqueId)) }
            toGeary().encodeComponentsTo(this)
            player.playerData.recentInteractEntity = uniqueId
        } ?: return

        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        player.playSound(newLocation, Sound.ENTITY_ARMOR_STAND_PLACE, 1f, 1f)

        when (lockState) {
            true -> player.success("This Armor Stand is now protected!")
            false -> player.error("Use <b>/mia lock toggle</b> to protect this Armor Stand.")
        }
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractLockedArmorStand() {
        val lockedDisplay = rightClicked.lockedDisplay ?: return

        if (rightClicked !is ArmorStand) return
        if (lockedDisplay.owner == player.uniqueId && player.isSneaking)
            player.playerData.recentInteractEntity = rightClicked.uniqueId
        if (lockedDisplay.hasAccess(player)) return

        player.error("You do not have access to interact with this ${rightClicked.name}!")
        isCancelled = true
    }

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractItemFrame() {
        val frame = rightClicked.lockedDisplay ?: return
        if (rightClicked !is ItemFrame || frame.owner != player.uniqueId) return
        player.playerData.recentInteractEntity = rightClicked.uniqueId
    }

    @EventHandler
    fun PlayerItemFrameChangeEvent.onChangingLockedItemFrame() {
        if (itemFrame.lockedDisplay?.hasAccess(player) == false) {
            player.error("You do not have access to interact with this ${itemFrame.name}!")
            isCancelled = true
            return
        }
    }

    @EventHandler
    fun ProjectileHitEvent.onBreakingItemFrame() {
        val lockedDisplay = (hitEntity as? ItemFrame)?.lockedDisplay ?: return
        val attacker = entity.shooter ?: return

        if (attacker !is Player) isCancelled = true
        else if (lockedDisplay.hasAccess(attacker)) {
            attacker.error("You do not have access to interact with this ${hitEntity?.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun HangingBreakEvent.onBreakLockedFrame() {
        if ((entity as? ItemFrame)?.lockedDisplay?.lockState != true) return
        if (cause != HangingBreakEvent.RemoveCause.ENTITY) isCancelled = true
    }

    @EventHandler
    fun HangingBreakByEntityEvent.onBreakLockedFrame() {
        val frame = (entity as? ItemFrame)?.lockedDisplay ?: return
        val player = remover as? Player ?: return

        if (!frame.hasAccess(player)) {
            player.error("You do not have access to interact with this ${entity.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingArmorStand() {
        val armorStand = entity.lockedDisplay ?: return
        if (!armorStand.lockState) return
        val attacker = when (damager) {
            is Projectile -> (damager as Projectile).shooter.let { it as? Player ?: it as? Skeleton }
            is Player -> (damager as Player)
            else -> return
        }

        // Prevent non-players from breaking armor stands
        if (attacker !is Player) isCancelled = true
        else if (!armorStand.hasAccess(attacker)) {
            attacker.error("You do not have access to interact with this ${entity.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityDamageByBlockEvent.onBurningArmorStand() {
        val armorStand = entity.lockedDisplay ?: return
        if (!armorStand.lockState) return
        if (cause != EntityDamageEvent.DamageCause.LAVA && cause != EntityDamageEvent.DamageCause.FIRE_TICK) return
        isCancelled = true
    }

    @EventHandler
    fun PlayerFishEvent.onFishArmorstand() {
        val armorStandBukkit = caught as? ArmorStand ?: return
        val lockedDisplay = armorStandBukkit.lockedDisplay ?: return

        if (state != PlayerFishEvent.State.CAUGHT_ENTITY) return
        if (!lockedDisplay.hasAccess(player)) {
            player.error("You do not have access to this ${(caught as ArmorStand).name}")
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

        blocksThePistonChanges.forEach { blockinblocks ->
            // Get all armor stands in the blocks the piston changes
            blockinblocks.location.getNearbyEntitiesByType(ArmorStand::class.java, 1.0).forEach forEachArmorStand@{
                // If the armor stand is not locked, continue
                if (it.lockedDisplay?.lockState == true) return true
            }
        }
        return false
    }

    @EventHandler
    fun BlockPistonExtendEvent.onPistonExtend() {
        if (onPistonExtendRetract(block, direction, blocks)) isCancelled = true
    }

    @EventHandler
    fun BlockPistonRetractEvent.onPistonRetract() {
        if (onPistonExtendRetract(block, direction.oppositeFace, blocks)) isCancelled = true
    }
}
