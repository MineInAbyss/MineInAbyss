package com.mineinabyss.moderation

import com.mineinabyss.components.moderation.isInHelperMode
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent

class HelperListener : Listener {

    @EventHandler
    fun PlayerAttemptPickupItemEvent.onPickup() {
        if (player.isInHelperMode) isCancelled = true
    }

    @EventHandler
    fun PlayerDropItemEvent.onDrop() {
        if (player.isInHelperMode) isCancelled = true
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        if (player.isInHelperMode) player.gameMode = GameMode.SPECTATOR
    }
}
