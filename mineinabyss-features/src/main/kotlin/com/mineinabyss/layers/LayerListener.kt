package com.mineinabyss.layers

import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.services.PlayerManager
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.mineinabyss.core.layer
import net.kyori.adventure.title.Title
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class LayerListener : Listener {
    @EventHandler
    private fun PlayerAscendEvent.onPlayerAscend() = sendTitleOnLayerChange()

    @EventHandler(ignoreCancelled = true)
    private fun PlayerDescendEvent.onPlayerDescend() = sendTitleOnLayerChange()

    private fun PlayerChangeSectionEvent.sendTitleOnLayerChange() {
        if (PlayerManager.playerCanTeleport(player)) {
            val fromLayer = fromSection.layer ?: return
            val toLayer = toSection.layer ?: return

            if (fromLayer != toLayer) {
                player.showTitle(Title.title(toLayer.name.miniMsg(), toLayer.sub.miniMsg(), Title.Times.times(
                    2.5.seconds.toJavaDuration(),
                    0.5.seconds.toJavaDuration(),
                    1.seconds.toJavaDuration()
                )))
            }
        }
    }

    @EventHandler
    fun PlayerDeathEvent.appendLayerToDeathMessage() {
        val section = player.location.section ?: return
        val layerOfDeath = section.layer ?: return
        apply {
            //TODO translatable key for dying in layer
            deathMessage(deathMessage()?.append(" ${layerOfDeath.deathMessage}".miniMsg()))
        }
    }
}
