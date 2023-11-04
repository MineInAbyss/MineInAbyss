package com.mineinabyss.features.custom_hud

import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.ehhthan.happyhud.api.HudHolder
import com.mineinabyss.packy.components.packyData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class CustomHudListener(private val feature: CustomHudFeature) : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        toggleBackgroundLayouts(player, feature)
    }

/*    // Check if player has hud-pack, if not set isPackAccepted false
    // reloadLayouts will either add all defaults or just clear layouts
    @EventHandler
    fun PlayerJoinEvent.toggleHud() {
        val hudHolder = player.hudHolder ?: return
        hudHolder.isPackAccepted = feature.customHudTemplate in player.packyData.enabledPackAddons.map { it.id }
        hudHolder.reloadLayouts()
    }*/
}
