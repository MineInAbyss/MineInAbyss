package com.mineinabyss.features.custom_hud

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.packy.config.packy
import io.lumine.mythichud.api.HudHolder
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status
import org.bukkit.persistence.PersistentDataType

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
            val hudHolder = player.hudHolder ?: HudHolder(player)
            mythicHud.createBarHandler(hudHolder)
            hudHolder.initialize()
            toggleBackgroundLayouts(player, feature)
        } else {
            val layoutContainer = player.persistentDataContainer.get(LAYOUT_KEY, PersistentDataType.TAG_CONTAINER) ?: return
            layoutContainer.keys.forEach { key ->
                layoutContainer.set(key, PersistentDataType.BOOLEAN, false)
            }
            player.persistentDataContainer.set(LAYOUT_KEY, PersistentDataType.TAG_CONTAINER, layoutContainer)
        }
    }

}
