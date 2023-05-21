package com.mineinabyss.curse

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.helpers.handleCurse
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.mineinabyss.core.abyss
import com.mineinabyss.mobzy.modelengine.toModelEntity
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
        handleCurse(player, player.location, vehicle.location)
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
        if (!abyss.isModelEngineEnabled || !abyss.isMobzyEnabled) return
        val mount = entity.toModelEntity()?.mountManager ?: return
        if (entity.toGearyOrNull() == null) return

        if (mount.driver != null && mount.driver is Player)
            handleCurse((mount.driver as Player), from, to)
        mount.passengers.keys.filterIsInstance<Player>().forEach { passenger ->
            handleCurse(passenger, from, to)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerTeleportEvent.handleCurseOnTeleport() {
        val (player, from, to) = this
        if (this.cause == ENDER_PEARL || this.cause == CHORUS_FRUIT)
            handleCurse(player, from, to)
    }
}
