package com.mineinabyss.curse

import com.mineinabyss.components.playerData
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import com.mineinabyss.mineinabyss.core.isAbyssWorld
import com.mineinabyss.mineinabyss.core.layer
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import java.util.*

class CurseAscensionListener : Listener {
    private val recentlyMovedPlayers: MutableSet<UUID> = HashSet()

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

    @EventHandler(ignoreCancelled = true)
    fun PlayerTeleportEvent.handleCurseOnTeleport() {
        val (player, from, to) = this
        if (this.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL || this.cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)
            handleCurse(player, from, to)
    }

    private fun handleCurse(player: Player, from: Location, to: Location) {
        //Arbitrary range with the purpose of preventing curse on section change
        if (from.distanceSquared(to) > 32 * 32) return

        if (recentlyMovedPlayers.contains(player.uniqueId)) {
            recentlyMovedPlayers.remove(player.uniqueId)
            return
        }

        if (!player.world.isAbyssWorld) return

        val changeY = to.y - from.y
        val playerData = player.playerData

        playerData.apply {
            if (player.isInvulnerable) {
                curseAccrued = 0.0
            } else if (playerData.isAffectedByCurse) {
                val layer = to.layer ?: return

                val dist = curseAccrued
                curseAccrued = (dist + changeY).coerceAtLeast(0.0)
                if (dist >= 10) {
                    layer.ascensionEffects.forEach {
                        it.clone().applyEffect(player, 10)
                    }
                    curseAccrued -= 10
                }
            }
        }
    }
}
