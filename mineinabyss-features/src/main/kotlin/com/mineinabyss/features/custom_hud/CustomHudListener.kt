package com.mineinabyss.features.custom_hud

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.mineinabyss.features.abyss
import com.mineinabyss.geary.papermc.tracking.entities.events.GearyEntityAddToWorldEvent
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.packy.config.packy
import io.lumine.mythichud.api.HudHolder
import io.lumine.mythichud.api.MythicHUD
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status
import kotlin.time.Duration.Companion.seconds

class CustomHudListener(private val feature: CustomHudFeature) : Listener {

    @EventHandler
    fun PlayerResourcePackStatusEvent.onResourcepackLoad() {
        if (status == Status.ACCEPTED || status == Status.DOWNLOADED) return
        packy.plugin.launch {
            do {
                delay(1.ticks)
                if (player.isOnline) handleStatusEvent()
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
