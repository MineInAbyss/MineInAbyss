package com.mineinabyss.armorstandlock

import com.mineinabyss.components.armorstandlock.LockArmorStand
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.geary.papermc.store.encodeComponentsTo
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.spawning.spawn
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

class ArmorStandLockingListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.onPlacingArmorStand() {
        if (player.inventory.itemInMainHand.type != Material.ARMOR_STAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (blockFace == BlockFace.DOWN) return
        isCancelled = true

        val loc = player.getLastTwoTargetBlocks(null, 6)

        // Wacky code to mimic vanilla placing
        val newLoc =
            if (blockFace == BlockFace.UP)
                loc.last()?.location?.toCenterLocation()?.apply { y += 0.5 } ?: return
            else
                loc.first()?.location?.toCenterLocation()?.apply { y -= 0.5 } ?: return

        newLoc.spawn<ArmorStand>()?.apply {
            setRotation(player.location.yaw - 180, 0.0F)
            toGearyOrNull()?.setPersisting(LockArmorStand(player.uniqueId, false, mutableSetOf(player.uniqueId)))
            this.toGearyOrNull()?.encodeComponentsTo(this)
            player.playerData.recentRightclickedEntity = this
        } ?: return

        if (player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.subtract()
        player.playSound(newLoc, Sound.ENTITY_ARMOR_STAND_PLACE, 1f, 1f)
        player.error("Use /mia lock to edit who can interact with this armor stand")
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractLockedArmorStand() {
        val armorStand = rightClicked.toGeary().get<LockArmorStand>() ?: return

        if (armorStand.owner == player.uniqueId && player.inventory.itemInMainHand.type == Material.AIR)
            player.playerData.recentRightclickedEntity = rightClicked

        if (!armorStand.lockState) return
        if (!armorStand.isAllowed(player.uniqueId) && !player.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            player.error("You do not have access to interacting with this armor stand!")
            isCancelled = true
            return
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingArmorStand() {
        val armorStand = entity.toGeary().get<LockArmorStand>() ?: return

        if (!armorStand.lockState) return

        val attacker: Player =
            when (damager) {
                is Projectile -> {
                    (damager as Projectile).shooter as? Player ?: return
                }
                is Player -> {
                    (damager as Player)
                }
                else -> {
                    return
                }
            }

        if (!armorStand.isAllowed(attacker.uniqueId) && !attacker.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            attacker.error("You do not have access to interacting with this armor stand!")
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityMoveEvent.onEntityMove() {
        /**
         * This listener is used to prevent gravity and water from affecting armor stands.
         * This is done by setting the entity's velocity to 0 and cancelling the event.
         */
        if (entityType != EntityType.ARMOR_STAND) return

        val armorStand = entity.toGeary().get<LockArmorStand>() ?: return

        if (!armorStand.lockState) return

        // We only care about ArmorStands that have fishing hooks as passengers
        if (entity.passengers.none { it.type == EntityType.FISHING_HOOK }) return

        entity.velocity = Vector(0.0, 0.0, 0.0)
        isCancelled = true
    }

    @EventHandler
    fun PlayerFishEvent.onFishArmorstand() {
        /**
         * This listener is used to prevent players from fishing armor stands if they are not allowed to.
         * This is done by simply cancelling the event. A player can fish an armor stand if they are allowed to via /mia lock
         * or if they have the permission to bypass the lock.
         */
        if (state != PlayerFishEvent.State.CAUGHT_ENTITY) return // We only care about catching entities

        if (caught == null) return // We only care about non-null caught entities
        if (caught!!.type != EntityType.ARMOR_STAND) return // We only care about armor stands

        val armorStandEntity = caught as ArmorStand
        val armorStand = armorStandEntity.toGeary().get<LockArmorStand>() ?: return

        if (!armorStand.lockState) return

        // The player is allowed to fish if they are the owner of the armor stand or if they have the permission to bypass the lock

        if (!armorStand.isAllowed(player.uniqueId) && !player.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            player.error("You do not have access to fishing this armor stand!")
            hook.remove()
            isCancelled = true
        }
    }

    private fun onPistonExtendRetract(block: Block, direction: BlockFace, blocks: List<Block>): Boolean {
        /**
         * This function is used to prevent pistons from extending armor stands,
         * to prevent pistons from pushing blocks into armor stands,
         * and to prevent pistons from pushing armor stands using honey blocks below them.
         */

        val blocksThePistonChanges = mutableListOf<Block>()

        blocksThePistonChanges.add(block.location.add(direction.direction).block)

        for (blockinblocks in blocks) {
            // Gather all blocks that the piston will probably change in a list
            blocksThePistonChanges.add(blockinblocks.location.add(direction.direction).block)

            if (blockinblocks.type == Material.HONEY_BLOCK) {
                // If the piston is pushing honey blocks, we need to check if the honey block is below the armor stand
                blocksThePistonChanges.add(blockinblocks.location.add(BlockFace.UP.direction).block)
            }
        }

        for (blockinblocks in blocksThePistonChanges) {
            // Get all armor stands in the blocks the piston changes
            val armorStands = blockinblocks.location.getNearbyEntitiesByType(ArmorStand::class.java, 1.0)
            for (armorStand in armorStands) {
                // If any armorstand is locked, cancel the event
                val gearyArmorStand = armorStand.toGeary().get<LockArmorStand>() ?: continue
                if (!gearyArmorStand.lockState) continue

                return true
            }
        }

        // Everything is fine, the piston can move :)

        return false
    }

    @EventHandler
    fun BlockPistonExtendEvent.onPistonExtend() {
        /**
         * This listener is used to prevent pistons from pushing blocks into armor stands.
         * It calls the onPistonExtendRetract function to check if the piston is pushing blocks into armor stands.
         */
        if (onPistonExtendRetract(block, direction, blocks)) {
            isCancelled = true
        }
    }

    @EventHandler
    fun BlockPistonRetractEvent.onPistonRetract() {
        /**
         * This listener is used to prevent pistons from pushing blocks into armor stands.
         * It calls the onPistonExtendRetract function to check if the piston is pushing blocks into armor stands.
         */
        if (onPistonExtendRetract(
                block,
                direction.oppositeFace,
                blocks
            )
        ) { // We use the opposite direction because the piston is retracting
            isCancelled = true
        }
    }
}
