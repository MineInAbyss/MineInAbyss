package com.mineinabyss.features.overlay

import io.papermc.paper.event.player.PlayerArmSwingEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

object OverlayListener : Listener {
    @EventHandler
    fun PlayerInteractEvent.onClick() {
        if (Overlays.session(player) == null) return
        isCancelled = true
        Overlays.click(player)
    }

    @EventHandler
    fun PlayerArmSwingEvent.onSwing() {
        if (Overlays.session(player) == null) return
        Overlays.click(player)
    }

    @EventHandler
    fun PlayerToggleSneakEvent.onSneak() {
        if (isSneaking) Overlays.close(player)
    }

    @EventHandler
    fun EntityDamageEvent.onSuffocate() {
        if (cause == EntityDamageEvent.DamageCause.SUFFOCATION && Overlays.session(entity as? Player ?: return) != null) isCancelled = true
    }

    @EventHandler
    fun EntityDismountEvent.onDismount() {
        val player = entity as? Player ?: return
        if (dismounted == Overlays.session(player)?.camera) Overlays.close(player)
    }

    @EventHandler
    fun PlayerTeleportEvent.onTeleport() {
        if (Overlays.session(player) != null && (from.world != to.world || from.distanceSquared(to) > 4.0)) Overlays.close(player)
    }

    @EventHandler
    fun PlayerChangedWorldEvent.onChangeWorld() = Overlays.close(player)

    @EventHandler
    fun InventoryOpenEvent.onOpenInventory() {
        Overlays.close(player as? Player ?: return)
    }

    // The player's own inventory opens client side, the first click in it is the earliest signal
    @EventHandler
    fun InventoryClickEvent.onClickInventory() {
        val player = (whoClicked as? Player)?.takeIf { Overlays.session(it) != null } ?: return
        isCancelled = true
        Overlays.close(player)
    }

    @EventHandler
    fun InventoryDragEvent.onDragInventory() {
        val player = (whoClicked as? Player)?.takeIf { Overlays.session(it) != null } ?: return
        isCancelled = true
        Overlays.close(player)
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.onSwapHands() {
        if (Overlays.session(player) != null) isCancelled = true
    }

    @EventHandler
    fun PlayerDropItemEvent.onDrop() {
        if (Overlays.session(player) != null) isCancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() = Overlays.close(player)

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() = Overlays.close(player)

    @EventHandler
    fun PlayerJoinEvent.onJoin() = Overlays.clearIfStale(player)

    @EventHandler
    fun PlayerQuitEvent.onQuit() = Overlays.close(player)
}
