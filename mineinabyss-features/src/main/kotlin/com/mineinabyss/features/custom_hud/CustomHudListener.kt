package com.mineinabyss.features.custom_hud

import com.ehhthan.happyhud.api.HudHolder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status

class CustomHudListener(private val feature: CustomHudFeature) : Listener {

    @EventHandler
    fun PlayerResourcePackStatusEvent.onResourcepackLoad() {
        if (feature.customHudEnabled(player) && status == Status.SUCCESSFULLY_LOADED) {
            HudHolder.create(player, true)
            toggleBackgroundLayouts(player, feature)
        } else player.hudHolder?.disable()
    }

}
