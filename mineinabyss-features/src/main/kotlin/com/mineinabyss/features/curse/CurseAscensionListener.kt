package com.mineinabyss.features.curse

import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.features.helpers.forgetRecentSectionChange
import com.mineinabyss.features.helpers.handleCurse
import com.mineinabyss.features.helpers.markRecentSectionChange
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.ENDER_PEARL
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
    fun EntityMoveEvent.handleCurseOnPassengers() {
        if (entity.passengers.isNotEmpty()) {
            entity.passengers.filterIsInstance<Player>().forEach { passenger ->
                handleCurse(passenger, from, to)
            }
        }
    }

    /*@EventHandler
    fun EntityMoveEvent.onRidableModelEngineAscend() {
        if (!abyss.isModelEngineEnabled || !abyss.isMobzyEnabled) return
        val mount = entity.toModelEntity()?.mount ?: return
        if (entity.toGearyOrNull() == null) return

        if (mount.driver != null && mount.driver is Player)
            handleCurse((mount.driver as Player), from, to)
        mount.passengers.keys.filterIsInstance<Player>().forEach { passenger ->
            handleCurse(passenger, from, to)
        }
    }*/

    @EventHandler(ignoreCancelled = true)
    fun PlayerTeleportEvent.handleCurseOnTeleport() {
        val (player, from, to) = this
        if (this.cause == ENDER_PEARL || this.cause == PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT)
            handleCurse(player, from, to)
    }

    // DeeperWorld teleports across sections, that movement is not ascent and must not accrue curse
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerAscendEvent.markSectionChangeOnAscend() = markRecentSectionChange(player)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun PlayerDescendEvent.markSectionChangeOnDescend() = markRecentSectionChange(player)

    @EventHandler
    fun PlayerQuitEvent.clearSectionChangeOnQuit() = forgetRecentSectionChange(player)
}
