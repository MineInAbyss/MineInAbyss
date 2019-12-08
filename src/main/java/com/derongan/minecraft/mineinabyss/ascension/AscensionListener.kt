package com.derongan.minecraft.mineinabyss.ascension

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.world.WorldManager
import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.MineInAbyss
import com.derongan.minecraft.mineinabyss.getPlayerData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*
import kotlin.math.abs

class AscensionListener(private val context: AbyssContext) : Listener {
    private val recentlyMovedPlayers: MutableSet<UUID> = HashSet()
    private val worldManager: WorldManager? = Bukkit.getServicesManager().load(WorldManager::class.java)

    @EventHandler(ignoreCancelled = true)
    fun onPlayerMove(moveEvent: PlayerMoveEvent) {
        val player = moveEvent.player
        if (recentlyMovedPlayers.contains(player.uniqueId)) {
            recentlyMovedPlayers.remove(player.uniqueId)
            return
        }

        val manager = context.worldManager
        if (!manager.isAbyssWorld(player.world)) return

        val from = moveEvent.from
        val to = moveEvent.to!!
        val changeY = to.y - from.y
        val playerData = context.getPlayerData(player)

        if (playerData.isAffectedByCurse) {
            val dist = playerData.distanceAscended
            playerData.distanceAscended = (dist + changeY).coerceAtLeast(0.0)
            if (dist >= 10) {
                val layerForSection = manager.getLayerForSection(worldManager!!.getSectionFor(moveEvent.from))
                layerForSection.ascensionEffects.forEach { it.build().applyEffect(player, 10) }
                playerData.distanceAscended = 0.0
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun onPlayerAscend(e: PlayerAscendEvent) {
        onPlayerChangeSection(e)
    }

    @EventHandler(ignoreCancelled = true)
    private fun onPlayerDescend(e: PlayerDescendEvent) {
        onPlayerChangeSection(e)
    }

    private fun onPlayerChangeSection(changeSectionEvent: PlayerChangeSectionEvent) {
        val player = changeSectionEvent.player
        if (!context.getPlayerData(player).isAnchored) {
            recentlyMovedPlayers.add(player.uniqueId)
            val manager = context.worldManager
            val fromSection = changeSectionEvent.fromSection
            val toSection = changeSectionEvent.toSection
            val fromLayer = manager.getLayerForSection(fromSection)
            val toLayer = manager.getLayerForSection(toSection)
            val playerData = getPlayerData(player)

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
        val manager = context.worldManager
        val layerOfDeath = manager.getLayerForSection(worldManager!!.getSectionFor(player.location))
        deathEvent.deathMessage = deathEvent.deathMessage + layerOfDeath.deathMessage
    }
}