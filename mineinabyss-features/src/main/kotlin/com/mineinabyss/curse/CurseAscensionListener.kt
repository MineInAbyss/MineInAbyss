package com.mineinabyss.curse

import com.mineinabyss.geary.papermc.access.toGearyOrNull
import com.mineinabyss.helpers.handleCurse
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.mobzy.systems.systems.ModelEngineSystem.toModelEntity
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleMoveEvent

class CurseAscensionListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun PlayerMoveEvent.handleCurseOnMove() {
        handleCurse(player, from, to)
    }

    @EventHandler(ignoreCancelled = true)
    fun VehicleMoveEvent.handleCurseInVehicle() {
        vehicle.passengers.filterIsInstance<Player>().forEach { passenger ->
            handleCurse(passenger, from, to)
        }
    }

    @EventHandler
    fun VehicleEnterEvent.handleCurseOnVehicleEnter() {
        val player = entered as? Player ?: return
        handleCurse(player, from = player.location, to = vehicle.location)
    }

    @EventHandler
    fun EntityMoveEvent.handleCurseOnPassengers() {
        if (entity.passengers.isNotEmpty()) {
            entity.passengers.filterIsInstance<Player>().forEach { passenger ->
                handleCurse(passenger, from, to)
            }
        }
    }

    @EventHandler
    fun EntityMoveEvent.onRidableModelEngineAscend() {
        val mount = entity.toModelEntity()?.mountHandler ?: return
        if (entity.toGearyOrNull() == null) return

        if (mount.hasDriver() && mount.driver is Player)
            handleCurse((mount.driver as Player), from, to)
        mount.passengers["mount"]?.passengers?.filterIsInstance<Player>()?.forEach {
            handleCurse(it, from, to)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerTeleportEvent.handleCurseOnTeleport() {
        val (player, from, to) = this
        if (this.cause == ENDER_PEARL || this.cause == CHORUS_FRUIT)
            handleCurse(player, from, to)
    }
}
