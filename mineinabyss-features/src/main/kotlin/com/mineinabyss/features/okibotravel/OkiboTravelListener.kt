package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.okibotravel.menu.OkiboMainScreen
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class OkiboTravelListener(private val feature: OkiboTravelFeature) : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.onTalkToOkiboMan() {
        val okiboTraveler = rightClicked.toGearyOrNull()?.get<OkiboTraveler>() ?: return
        guiy { OkiboMainScreen(player, feature, okiboTraveler) }
    }
}
