package com.derongan.minecraft.mineinabyss.ascension

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.services.PlayerManager
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.Point
import com.derongan.minecraft.deeperworld.world.section.section
import com.derongan.minecraft.mineinabyss.playerData
import com.derongan.minecraft.mineinabyss.world.isAbyssWorld
import com.derongan.minecraft.mineinabyss.world.layer
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import java.util.*

object AscensionListener : Listener {
    private val recentlyMovedPlayers: MutableSet<UUID> = HashSet()

    @EventHandler(ignoreCancelled = true)
    fun onPlayerMove(moveEvent: PlayerMoveEvent) {
        val (player, from, to) = moveEvent
        handleCurse(player, from, to ?: return)
    }

    @EventHandler(ignoreCancelled = true)
    fun onMove(moveEvent: VehicleMoveEvent) {
        val (_, from, to) = moveEvent

        moveEvent.vehicle.passengers.filterIsInstance<Player>().forEach { passenger ->
            handleCurse(passenger, from, to)
        }
    }

    private fun handleCurse(player: Player, from: Location, to: Location) {
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
                //TODO this scaling mechanic has been found to confuse players due to lack of feedback to the user.
                // It also appears to be inaccurate with the show's explanation of the curse.
//                val curseFactor = getCurseStrength(to)

                val dist = curseAccrued
                curseAccrued = (dist /*+ curseFactor * changeY*/).coerceAtLeast(0.0)
                if (dist >= 10) {
                    layer.ascensionEffects.forEach {
                        it.build().applyEffect(player, 10)
                    }
                    curseAccrued -= 10
                }
            }
        }
    }

    private fun getCurseStrength(to: Location): Double {
        val section = WorldManager.getSectionFor(to) ?: return 1.0
        val layer = section.layer ?: return 0.0
        val reg = section.region

        val localCords = Point(to.x.toInt(), to.z.toInt()) - reg.center
        val distFromShaft = localCords.length

        val distFactor = ((distFromShaft - layer.maxCurseRadius) / (layer.minCurseRadius - layer.maxCurseRadius)).coerceIn(0.0, 1.0)
        var curseFactor = layer.maxCurseMultiplier - distFactor * (layer.maxCurseMultiplier - layer.minCurseMultiplier)

        var overridePri = 0
        for (r in layer.curseOverrideRegions) {
            if (r.region.contains(localCords) && r.priority > overridePri) {
                curseFactor = r.strength
                overridePri = r.priority
            }
        }
        return curseFactor.coerceAtLeast(0.0)
    }

    @EventHandler(ignoreCancelled = true)
    private fun onPlayerAscend(e: PlayerAscendEvent) = onPlayerChangeSection(e)

    @EventHandler(ignoreCancelled = true)
    private fun onPlayerDescend(e: PlayerDescendEvent) = onPlayerChangeSection(e)

    @EventHandler
    fun onEnterVehicle(e: VehicleEnterEvent) {
        val player = e.entered
        if (player is Player) {
            handleCurse(player, from = player.location, to = e.vehicle.location)
        }
    }

    private fun onPlayerChangeSection(changeSectionEvent: PlayerChangeSectionEvent) {
        val player = changeSectionEvent.player
        if (PlayerManager.playerCanTeleport(player)) {
            recentlyMovedPlayers.add(player.uniqueId)
            val fromSection = changeSectionEvent.fromSection
            val toSection = changeSectionEvent.toSection
            val fromLayer = fromSection.layer ?: return
            val toLayer = toSection.layer ?: return

            if (fromLayer !== toLayer) {
                player.sendTitle(toLayer.name, toLayer.sub, 50, 10, 20)
            }
        } else {
            changeSectionEvent.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(deathEvent: PlayerDeathEvent) {
        val (player) = deathEvent
        val section = player.location.section ?: return
        val layerOfDeath = section.layer ?: return
        deathEvent.apply {
            deathMessage += layerOfDeath.deathMessage
        }
    }
}