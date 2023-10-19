package com.mineinabyss.features.okibotravel

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.components.playerData
import com.mineinabyss.features.helpers.di.Features.okiboLine
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import kotlin.time.Duration.Companion.seconds

class OkiboTravelListener : Listener {

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onInteractMap() {
        if (hand != EquipmentSlot.HAND) return
        val destination = getHitboxStation(entityId)?.getStation ?: return
        val playerStation = okiboLine.config.allStations.filter { it != destination }.minByOrNull { it.location.distanceSquared(player.location) } ?: return player.error("You are not near a station!")
        val cost = playerStation.costTo(destination) ?: return player.error("You cannot travel to that station!")

        player.toGeary().with { traveler: OkiboTraveler ->
            when (traveler.selectedDestination) {
                destination -> {
                    when {
                        cost > player.playerData.orthCoinsHeld -> player.error("You do not have enough coins to travel to that station!")
                        cost == 0 -> player.error("You are already at that station!")
                        else -> {
                            //player.playerData.orthCoinsHeld -= cost
                            //spawnOkiboCart(player, playerStation, destination)
                        }
                    }
                    return
                }
                else -> player.toGeary().remove<OkiboTraveler>()
            }
        }

        player.info("<gold>You selected <yellow>${destination.name}</yellow> station!")
        player.info("<gold>The cost to travel there will be <yellow>$cost</yellow> Orth Coins.")
        player.info("<gold>Click the map again to confirm your selection.")
        player.toGeary().set(OkiboTraveler(destination))

        abyss.plugin.launch {
            delay(5.seconds)
            if (player.isOnline) player.toGeary().remove<OkiboTraveler>()
        }
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        player.toGeary().remove<OkiboTraveler>()
    }
}
