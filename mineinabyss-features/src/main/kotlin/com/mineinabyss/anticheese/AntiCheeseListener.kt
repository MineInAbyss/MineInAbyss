package com.mineinabyss.anticheese

import com.mineinabyss.helpers.handleCurse
import com.mineinabyss.helpers.isInHub
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.mineinabyss.core.layer
import com.mineinabyss.mineinabyss.core.mineInAbyss
import com.okkero.skedule.schedule
import dev.geco.gsit.api.GSitAPI
import dev.geco.gsit.api.event.PlayerGetUpSitEvent
import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.ShulkerBox
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
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
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
            player.error("${ChatColor.BOLD}Milk ${ChatColor.RED}has been disabled")
        } else if (cause != EntityPotionEffectEvent.Cause.PLUGIN && cause != EntityPotionEffectEvent.Cause.COMMAND) {
            if (newEffect?.type == PotionEffectType.DAMAGE_RESISTANCE) {
                isCancelled = true
                player.error("The ${ChatColor.BOLD}Resistance Effect ${ChatColor.RED}has been disabled")
            }
            if (newEffect?.type == PotionEffectType.SLOW_FALLING) {
                isCancelled = true
                player.error("${ChatColor.BOLD}Slow Falling ${ChatColor.RED}has been disabled")
            }
        }
    }

    @EventHandler
    fun BlockDispenseEvent.preventBackpackPlace() {
        if (item.type.toString().contains("SHULKER"))
            isCancelled = true
    }
}

@EventHandler
fun EntityDamageByEntityEvent.cancelMinecartTNT() {
    if (damager is ExplosiveMinecart) isCancelled = true

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
    fun PlayerGetUpSitEvent.handleCurseOnSitting() {
        handleCurse(player, seat.location.toBlockLocation(), player.location)
    }
}

