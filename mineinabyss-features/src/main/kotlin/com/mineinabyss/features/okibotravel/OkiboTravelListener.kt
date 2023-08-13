package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.okibotravel.menu.OkiboMainScreen
import com.mineinabyss.features.okibotravel.menu.spawnOkiboCart
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.textcomponents.miniMsg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

class OkiboTravelListener(private val feature: OkiboTravelFeature) : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.onTalkToOkiboMan() {
        val okiboTraveler = rightClicked.toGearyOrNull()?.get<OkiboTraveler>() ?: return
        guiy { OkiboMainScreen(player, feature, okiboTraveler) }
    }

    @EventHandler
    fun PlayerInteractEntityEvent.onClickMap() {
        val destination = rightClicked.toGearyOrNull()?.get<OkiboLineStation>() ?: return
        player.sendMessage("You clicked the map at station ${destination.name}!".miniMsg())
        val playerStation = okiboLine.config.okiboStations.filter { it != destination }.minByOrNull { it.location.distanceSquared(player.location) } ?: return player.error("You are not near a station!")
        spawnOkiboCart(player, playerStation, destination)
    }
}
