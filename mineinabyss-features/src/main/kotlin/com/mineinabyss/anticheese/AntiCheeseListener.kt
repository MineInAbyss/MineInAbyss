package com.mineinabyss.anticheese

import com.mineinabyss.components.npc.patreonshop.PatreonItem
import com.mineinabyss.helpers.handleCurse
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.looty.tracking.toGearyOrNull
import com.mineinabyss.mineinabyss.core.layer
import dev.geco.gsit.api.GSitAPI
import dev.geco.gsit.api.event.PlayerGetUpSitEvent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

class AntiCheeseListener: Listener {
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

    // Temporary fix for bugged items dropped by bugged Mobzy
    @EventHandler
    fun InventoryClickEvent.removeBuggedWeapons() {
        val item = currentItem ?: return
        if (item.type == Material.DIAMOND_SWORD && item.itemMeta?.hasCustomModelData() == true) {
            if (item.toGearyOrNull(whoClicked as Player)?.has<PatreonItem>() == true) return
            currentItem = ItemStack(Material.DIAMOND_SWORD)
        }

        if (item.type == Material.STONE && item.itemMeta.hasEnchant(Enchantment.DEPTH_STRIDER)) {
            currentItem = ItemStack(Material.STONE)
        }
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
