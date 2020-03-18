package com.derongan.minecraft.mineinabyss.ascension

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.world.Point
import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.playerData
import com.derongan.minecraft.mineinabyss.world.AbyssWorldManager
import com.derongan.minecraft.mineinabyss.abyssContext
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import java.util.*

class AscensionListener : Listener {
    private val recentlyMovedPlayers: MutableSet<UUID> = HashSet()
    private val worldManager: WorldManager? = Bukkit.getServicesManager().load(WorldManager::class.java)




    @EventHandler(ignoreCancelled = true)
    fun onPlayerMove(moveEvent: PlayerMoveEvent) {
        val player = moveEvent.player
        val from = moveEvent.from
        val to = moveEvent.to?:return

        handleCurse(player, to, from)
    }

    @EventHandler(ignoreCancelled = true)
    fun onMove(moveEvent: VehicleMoveEvent) {
        val from = moveEvent.from
        val to = moveEvent.to?:return

        for (passenger in moveEvent.vehicle.passengers){
            if(passenger!=null && passenger is Player){
                //compiler is dumb
                val player = passenger as Player
                handleCurse(player, to, from)
            }
        }
    }

    private fun handleCurse(player: Player, to: Location, from: Location) {
        if (recentlyMovedPlayers.contains(player.uniqueId)) {
            recentlyMovedPlayers.remove(player.uniqueId)
            return
        }

        val manager = abyssContext.worldManager
        if (!manager.isAbyssWorld(player.world)) return


        val changeY = to.y - from.y
        val playerData = player.playerData

        if (player.isInvulnerable) {
            playerData.curseAccrued = 0.0
        } else if (playerData.isAffectedByCurse) {
            val section = worldManager!!.getSectionFor(to) ?: return
            val layer = manager.getLayerForSection(section)
            val curseFactor = getCurseStrength(manager, to)

            val dist = playerData.curseAccrued
            playerData.curseAccrued = (dist + curseFactor * changeY).coerceAtLeast(0.0)
            if (dist >= 10) {


                layer.ascensionEffects.forEach {

                    it.build().applyEffect(player, 10)
                }
                playerData.curseAccrued -= 10
            }
        }
    }

    private fun getCurseStrength(manager: AbyssWorldManager, to: Location): Double {
        val section = worldManager!!.getSectionFor(to) ?: return 1.0
        val layer = manager.getLayerForSection(section)
        val reg = section.region

        val localCords = Point(to) - reg.midPoint()
        val distFromShaft = localCords.length()

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
    fun onEnterVehicle(e: VehicleEnterEvent){
        val player = e.entered
        if(player is Player) {
            handleCurse(player, player.location, e.vehicle.location)
        }
    }

    private fun onPlayerChangeSection(changeSectionEvent: PlayerChangeSectionEvent) {
        val player = changeSectionEvent.player
        if (!abyssContext.getPlayerData(player).isAnchored) {
            recentlyMovedPlayers.add(player.uniqueId)
            val manager = abyssContext.worldManager
            val fromSection = changeSectionEvent.fromSection
            val toSection = changeSectionEvent.toSection
            val fromLayer = manager.getLayerForSection(fromSection)
            val toLayer = manager.getLayerForSection(toSection)
            val playerData = player.playerData

            if (toSection.toString() == MineInAbyss.getContext().config["hub-section"] && playerData.isIngame) {
                player.sendTitle("${ChatColor.RED}Cannot return to Orth during a run!", "Use /leave instead", 5, 40, 20)
                changeSectionEvent.isCancelled = true
                player.velocity = player.velocity.setY(-0.5)
            } else if (fromLayer !== toLayer) {
                player.sendTitle(toLayer.name, toLayer.sub, 50, 10, 20)
            }
        } else {
            changeSectionEvent.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(deathEvent: PlayerDeathEvent) {
        val player = deathEvent.entity
        val manager = abyssContext.worldManager
        val section = worldManager!!.getSectionFor(player.location)
        val layerOfDeath = section?.let { manager.getLayerForSection(section) }
        deathEvent.deathMessage = deathEvent.deathMessage + (layerOfDeath?.deathMessage ?: "")
    }
}