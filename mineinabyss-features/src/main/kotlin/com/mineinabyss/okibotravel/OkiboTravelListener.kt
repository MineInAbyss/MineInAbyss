package com.mineinabyss.okibotravel

import com.mineinabyss.components.okibotravel.OkiboTravel
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.guiy.inventory.guiy
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class OkiboTravelListener(private val feature: OkiboTravelFeature) : Listener {

    @EventHandler
    fun PlayerInteractAtEntityEvent.onTalkToOkiboMan() {
        rightClicked.toGearyOrNull()?.get<OkiboTravel>() ?: return
        guiy { OkiboTravelMenu(player, feature) }
    }
}
