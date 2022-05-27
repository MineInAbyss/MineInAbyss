package com.mineinabyss.anticheese

import com.mineinabyss.helpers.handleCurse
import com.mineinabyss.helpers.isInHub
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.layer
import dev.geco.gsit.api.GSitAPI
import dev.geco.gsit.api.event.PlayerGetUpSitEvent
import dev.geco.gsit.api.event.PlayerSitEvent
import org.bukkit.block.Dispenser
import org.bukkit.block.data.Directional
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.minecart.ExplosiveMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.potion.PotionEffectType

class AntiCheeseListener : Listener {

    @EventHandler
    fun BlockPlaceEvent.preventPlacement() {
        if (player.location.layer?.blockBlacklist?.contains(blockPlaced.type) == true) {
            player.error("You may not place this block on this layer.")
            isCancelled = true
        }
    }

    @EventHandler
    fun EntityPotionEffectEvent.onPlayerHit() {
        val player = entity as? Player ?: return
        if (cause == EntityPotionEffectEvent.Cause.MILK) {
            isCancelled = true
            player.error("<b>Milk</b> has been disabled")
        } else if (cause != EntityPotionEffectEvent.Cause.PLUGIN && cause != EntityPotionEffectEvent.Cause.COMMAND) {
            if (newEffect?.type == PotionEffectType.DAMAGE_RESISTANCE) {
                isCancelled = true
                player.error("The <b>Resistance Effect</b> has been disabled")
            }
            if (newEffect?.type == PotionEffectType.SLOW_FALLING) {
                isCancelled = true
                player.error("<b>Slow Falling</b> has been disabled")
            }
        }
    }

    @EventHandler
    fun BlockDispenseEvent.preventBackpackPlace() {
        if ("SHULKER" in item.type.toString()) {
            val inv = (block.state as Dispenser).inventory
            val relative = block.getRelative((block.blockData as Directional).facing)
            if (relative.isSolid || !relative.isReplaceable) return

            for (i in inv.contents!!) {
                if (i != null && i == item) {
                    i.subtract(1)
                    break
                }
            }
            isCancelled = true

            block.world.dropItemNaturally(relative.location, item)
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.cancelMinecartTNT() {
        if (damager is ExplosiveMinecart) isCancelled = true
    }

    // Cancels moving entities with fishing rods in Orth
    @EventHandler
    fun PlayerFishEvent.cancelBlockGrief() {
        if (caught?.type != EntityType.PLAYER && player.isInHub()) isCancelled = true
    }
}

class GSitListener : Listener {

    // Cancels pistons if a player is riding it via a GSit Seat
    @EventHandler
    fun BlockPistonExtendEvent.seatMovedByPiston() {
        if (GSitAPI.getSeats(blocks).isEmpty()) return
        else isCancelled = true
    }

    @EventHandler
    fun PlayerSitEvent.handleCurseOnSitting() {
        handleCurse(player, seat.location.toBlockLocation(), player.location)
    }

    @EventHandler
    fun PlayerGetUpSitEvent.handleCurseOnSitting() {
        handleCurse(player, player.location, seat.location.toBlockLocation())
    }
}

