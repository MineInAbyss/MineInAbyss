package com.mineinabyss.anticheese

import com.mineinabyss.helpers.handleCurse
import dev.geco.gsit.api.GSitAPI
import dev.geco.gsit.api.event.EntityGetUpSitEvent
import dev.geco.gsit.api.event.EntitySitEvent
import dev.geco.gsit.api.event.PreEntitySitEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent

class GSitListener : Listener {

    // Cancels pistons if a player is riding it via a GSit Seat
    @EventHandler
    fun BlockPistonExtendEvent.seatMovedByPiston() {
        if (GSitAPI.getSeats(blocks).isNotEmpty()) isCancelled = true
    }

    @EventHandler
    fun PreEntitySitEvent.onSitMidair() {
        if ((entity as? Player ?: return).fallDistance >= 4.0) isCancelled = true
    }

    @EventHandler
    fun EntitySitEvent.handleCurseOnSitting() {
        val player = (entity as? Player ?: return)
        handleCurse(player, seat.location.toBlockLocation(), player.location)
    }

    @EventHandler
    fun EntityGetUpSitEvent.handleCurseOnSitting() {
        val player = (entity as? Player ?: return)
        handleCurse(player, player.location, seat.location.toBlockLocation())
    }
}
