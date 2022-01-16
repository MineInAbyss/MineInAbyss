package com.mineinabyss.armorstandlock

import com.mineinabyss.components.armorstandlock.LockArmorStand
import com.mineinabyss.components.playerData
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.geary.minecraft.access.toGearyOrNull
import com.mineinabyss.geary.minecraft.store.encodeComponentsTo
import com.mineinabyss.idofront.messaging.error
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

class ArmorStandLockingListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.onPlacingArmorStand() {
        if (player.inventory.itemInMainHand.type != Material.ARMOR_STAND) return
        if (action != Action.RIGHT_CLICK_BLOCK) return
        if (hand != EquipmentSlot.HAND) return
        if (blockFace == BlockFace.DOWN) return
        isCancelled = true

        // Wacky code to mimic vanilla placing
        val newLoc =
            if (blockFace == BlockFace.UP)
                player.getTargetBlockExact(6)?.location!!.toCenterLocation().apply { y += 0.5 }
            else
                player.getLastTwoTargetBlocks(null, 6).first()?.location!!.toCenterLocation().apply { y -= 0.5 }

        val armorStand = newLoc.world.spawnEntity(newLoc, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.toGearyOrNull()?.setPersisting(LockArmorStand(player.uniqueId))
        armorStand.toGearyOrNull()?.encodeComponentsTo(armorStand)
        player.inventory.itemInMainHand.subtract()
        player.playSound(newLoc, Sound.ENTITY_ARMOR_STAND_PLACE, 1f, 1f)
    }

    @EventHandler
    fun PlayerInteractAtEntityEvent.onInteractLockedArmorStand() {
        val gearyEntity = rightClicked.toGeary().get<LockArmorStand>() ?: return

        if (!gearyEntity.allowedAccess.contains(player.uniqueId) && !player.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            player.error("You do not have access to interacting with this armor stand!")
            isCancelled = true
            return
        }
        if (gearyEntity.owner == player.uniqueId && player.inventory.itemInMainHand.type == Material.AIR) {
            player.error("Use /mia lock to edit who can interact with this armor stand")
            player.playerData.recentRightclickedEntity = rightClicked
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.onBreakingArmorStand() {
        val gearyEntity = entity.toGeary().get<LockArmorStand>() ?: return
        val player = damager as Player

        if (!gearyEntity.allowedAccess.contains(player.uniqueId) && !player.hasPermission("mineinabyss.lockarmorstand.bypass")) {
            player.error("You do not have access to interacting with this armor stand!")
            isCancelled = true
        }
    }
}
