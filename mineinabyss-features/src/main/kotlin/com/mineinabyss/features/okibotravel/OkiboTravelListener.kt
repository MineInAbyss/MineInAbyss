package com.mineinabyss.features.okibotravel

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features.okiboLine
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.papermc.features.common.cooldowns.Cooldown
import com.mineinabyss.geary.papermc.features.common.cooldowns.Cooldowns
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.time.Duration.Companion.seconds

class OkiboTravelListener : Listener {

    private val okiboMapCooldown = Cooldown(1.seconds, null, "mineinabyss:okibomap")

    @EventHandler
    fun PlayerChunkLoadEvent.onLoad() {
        abyss.plugin.launch {
            delay(2.ticks)
            val chunkKey = chunk.chunkKey
            val okiboMap = okiboLine.config.okiboMaps.firstOrNull {
                if (!it.location.isWorldLoaded || !it.location.isChunkLoaded) return@firstOrNull false
                it.location.chunk.chunkKey == chunkKey
            } ?: return@launch
            player.sendOkiboMap(okiboMap)
        }
    }

    @EventHandler
    fun PlayerChunkUnloadEvent.onUntrack() {
        val chunkKey = chunk.chunkKey
        val okiboMap = okiboLine.config.okiboMaps.firstOrNull {
            if (!it.location.isWorldLoaded || !it.location.isChunkLoaded) return@firstOrNull false
            it.location.chunk.chunkKey == chunkKey
        } ?: return
        player.removeOkiboMap(okiboMap)
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteractMap() {
        val gearyPlayer = player.toGeary().takeIf { hand == EquipmentSlot.HAND } ?: return
        if (!Cooldowns.isComplete(gearyPlayer, okiboMapCooldown.id)) return
        okiboMapCooldown.execute(ActionGroupContext(gearyPlayer))
        val destination = getHitboxStation(entityId)?.getStation ?: return
        val playerStation = okiboLine.config.allStations.filter { it != destination }.minByOrNull { it.location.distanceSquared(player.location) } ?: return player.error("You are not near a station!")
        val cost = playerStation.costTo(destination) ?: return player.error("You cannot travel to that station!")

        abyss.plugin.launch {
            delay(5.seconds)
            if (player.isOnline) gearyPlayer.remove<OkiboTraveler>()
        }

        gearyPlayer.with { traveler: OkiboTraveler ->
            when (traveler.selectedDestination) {
                destination -> {
                    player.editPlayerData {
                        when {
                            cost > orthCoinsHeld -> player.error("You do not have enough coins to travel to that station!")
                            playerStation == destination -> player.error("You are already at that station!")
                            else -> {
                                if (cost > 0) orthCoinsHeld -= cost
                                spawnOkiboCart(player, playerStation, destination)
                            }
                        }
                    }
                    return
                }
                else -> player.toGeary().remove<OkiboTraveler>()
            }
        }

        // Only confirm when there is a cost
        player.info("<gold>Do you want a ride to <i>${destination.displayName}</i>?")
        player.info("<gold>Click again to confirm!")
        gearyPlayer.set(OkiboTraveler(destination))
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.toGeary().remove<OkiboTraveler>()
    }
}
