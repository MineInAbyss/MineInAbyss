package com.mineinabyss.features.custom_hud

import com.ehhthan.happyhud.api.HudHolder
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status
import kotlin.time.Duration.Companion.seconds

class CustomHudListener(private val feature: CustomHudFeature) : Listener {

    @EventHandler
    fun PlayerResourcePackStatusEvent.onResourcepackLoad() {
        abyss.plugin.launch {
            delay(1.seconds)
            if (feature.customHudEnabled(player) && status == Status.SUCCESSFULLY_LOADED) {
                HudHolder.create(player, true)
                toggleBackgroundLayouts(player, feature)
            } else player.hudHolder?.disable()
        }
    }
}
