package com.mineinabyss.features.custom_hud

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.packy.components.packyData
import com.mineinabyss.packy.config.packy
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status

class CustomHudListener(private val feature: CustomHudFeature) : Listener {

//    @EventHandler
//    fun PlayerResourcePackStatusEvent.onResourcepackLoad() {
//        if (status == Status.ACCEPTED || status == Status.DOWNLOADED) return
//        packy.plugin.launch {
//            do {
//                delay(1.ticks)
//                if (player.isOnline) betterhud.getHudPlayer(player).isHudEnabled = feature.customHudTemplate in player.packyData.enabledPackIds
//            } while (player.isConnected && !player.isOnline)
//        }
//    }

    @EventHandler
    fun PlayerResourcePackStatusEvent.onResourcepackLoad() {
        if (status == Status.ACCEPTED || status == Status.DOWNLOADED) return
        packy.plugin.launch {
            do {
                delay(1.ticks)
                if (player.isOnline) handleStatusEvent()
                //if (player.isOnline) betterhud.getHudPlayer(player).isHudEnabled = feature.customHudTemplate in player.packyData.enabledPackIds
            } while (player.isConnected && !player.isOnline)
        }
    }

    private fun PlayerResourcePackStatusEvent.handleStatusEvent() {
        if (status == Status.SUCCESSFULLY_LOADED && feature.customHudEnabled(player)) {
            mythicHud.createBarHandler(player.hudHolder)
            player.hudHolder.initialize()
            toggleBackgroundLayouts(player, feature)
        } else mythicHud.createBarHandler(player.hudHolder).disable()
    }

}
