package com.mineinabyss.displayLocker

import com.mineinabyss.components.displaylocker.LockDisplayItem
import com.mineinabyss.components.displaylocker.lockedDisplay
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.spawning.spawn
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class DisplayLockerListener(val feature: DisplayLockerFeature) : Listener {
    @EventHandler
    fun HangingPlaceEvent.onPlaceItemFrame() {
        val frame = entity as? ItemFrame ?: return
        val player = player ?: return
        val geary = frame.toGeary()
        geary.getOrSetPersisting { LockDisplayItem(player.uniqueId, false, mutableSetOf(player.uniqueId)) }
        geary.encodeComponentsTo(frame)
        player.playerData.recentInteractEntity = frame.uniqueId
        player.error("Use <b>/mia lock toggle</b> to protect this Item Frame.")
    }

    @EventHandler
    fun PlayerInteractEvent.onPlaceArmorStand() { // pretty much the same code as before
        if (player.inventory.itemInMainHand.type != Material.ARMOR_STAND) return
        if (hand != EquipmentSlot.HAND || blockFace == BlockFace.DOWN || action != Action.RIGHT_CLICK_BLOCK) return
        isCancelled = true

        val initialLocation = player.getLastTwoTargetBlocks(null, 6)
        val newLocation =
            if (blockFace == BlockFace.UP) initialLocation.last()?.location?.toCenterLocation()?.apply { y += 0.5 }
                ?: return
            else initialLocation.first()?.location?.toCenterLocation()?.apply { y -= 0.5 } ?: return

        newLocation.spawn<ArmorStand>()?.apply {
            setRotation(player.location.yaw - 180, 0.0F)
            toGeary().getOrSetPersisting { LockDisplayItem(player.uniqueId, false, mutableSetOf(player.uniqueId)) }
            toGeary().encodeComponentsTo(this)
            player.playerData.recentInteractEntity = uniqueId
        } ?: return

        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        player.playSound(newLocation, Sound.ENTITY_ARMOR_STAND_PLACE, 1f, 1f)
        player.error("Use <b>/mia lock toggle</b> to protect this Armor Stand")
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractLockedArmorStand() {
        val armorStand = rightClicked.lockedDisplay ?: return

        if (rightClicked !is ArmorStand) return
        if (armorStand.owner == player.uniqueId && player.isSneaking)
            player.playerData.recentInteractEntity = rightClicked.uniqueId
        if (!armorStand.lockState || player.uniqueId in armorStand.allowedAccess || player.hasPermission(feature.bypassPermission)) return

        player.error("You do not have access to interact with this ${rightClicked.name}!")
        isCancelled = true
        return
    }

    @EventHandler
    fun PlayerInteractEntityEvent.onInteractItemFrame() {
        val frame = rightClicked.lockedDisplay ?: return
        if (rightClicked !is ItemFrame || frame.owner != player.uniqueId) return
        player.playerData.recentInteractEntity = rightClicked.uniqueId
    }

    @EventHandler
    fun PlayerItemFrameChangeEvent.onChangingLockedItemFrame() {
        val frame = itemFrame.lockedDisplay ?: return

        if (!frame.lockState) return
        if (player.uniqueId !in frame.allowedAccess && !player.hasPermission("mineinabyss.lockdisplay.bypass")) {
            player.error("You do not have access to interact with this ${itemFrame.name}!")
            isCancelled = true
            return
        }
    }

    @EventHandler
    fun ProjectileHitEvent.onBreakingItemFrame() {
        val frame = hitEntity?.lockedDisplay ?: return
        val attacker = entity.shooter as? Player ?: return

        if (!frame.lockState || hitEntity !is ItemFrame) return
        if (attacker.uniqueId !in frame.allowedAccess && !attacker.hasPermission("mineinabyss.lockdisplay.bypass")) {
            attacker.error("You do not have access to interact with this ${hitEntity?.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun HangingBreakEvent.onBreakLockedFrame() {
        val frame = entity.lockedDisplay ?: return
        if (!frame.lockState || entity !is ItemFrame) return
        if (cause != HangingBreakEvent.RemoveCause.ENTITY) isCancelled = true
    }

    @EventHandler
    fun HangingBreakByEntityEvent.onBreakLockedFrame() {
        val frame = entity.lockedDisplay ?: return
        val player = remover as? Player ?: return

        if (!frame.lockState || entity !is ItemFrame) return
        if (!frame.allowedAccess.contains(player.uniqueId)) {
            player.error("You do not have access to interact with this ${entity.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingArmorStand() {
        val armorStand = entity.lockedDisplay ?: return
        val attacker = when (damager) {
            is Projectile -> (damager as Projectile).shooter as? Player ?: return
            is Player -> (damager as Player)
            else -> return
        }

        if (!armorStand.lockState) return
        if (attacker.uniqueId !in armorStand.allowedAccess && !attacker.hasPermission("mineinabyss.lockdisplay.bypass")) {
            attacker.error("You do not have access to interact with this ${entity.name}")
            isCancelled = true
        }
    }

    @EventHandler
    fun PlayerFishEvent.onFishArmorstand() {
        val armorStandBukkit = caught as? ArmorStand ?: return
        val armorStandGeary = armorStandBukkit.lockedDisplay ?: return

        if (state != PlayerFishEvent.State.CAUGHT_ENTITY) return
        if (!armorStandGeary.lockState) return
        if (player.uniqueId !in armorStandGeary.allowedAccess && !player.hasPermission("mineinabyss.lockdisplay.bypass")) {
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

        for (blockinblocks in blocksThePistonChanges) {
            // Get all armor stands in the blocks the piston changes
            val armorStands = blockinblocks.location.getNearbyEntitiesByType(ArmorStand::class.java, 1.0)
            for (armorStand in armorStands) {
                // If any armorstand is locked, cancel the event
                val gearyArmorStand = armorStand.lockedDisplay ?: return false
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
