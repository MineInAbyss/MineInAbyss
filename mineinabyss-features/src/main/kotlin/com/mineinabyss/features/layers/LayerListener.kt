package com.mineinabyss.features.layers

import com.mineinabyss.deeperworld.event.PlayerAscendEvent
import com.mineinabyss.deeperworld.event.PlayerChangeSectionEvent
import com.mineinabyss.deeperworld.event.PlayerDescendEvent
import com.mineinabyss.deeperworld.services.PlayerManager
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.features.hubstorage.isInHub
import com.mineinabyss.idofront.textcomponents.miniMsg
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
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
                player.showTitle(
                    Title.title(
                        toLayer.name.miniMsg(), toLayer.sub.miniMsg(), Title.Times.times(
                            2.5.seconds.toJavaDuration(),
                            0.5.seconds.toJavaDuration(),
                            1.seconds.toJavaDuration()
                        )
                    )
                )
            }
        }
    }

    @EventHandler
    fun PlayerDeathEvent.appendLayerToDeathMessage() {
        val section = player.location.section ?: return
        val layerOfDeath = section.layer ?: return
        deathMessage(deathMessage()?.append(" ${layerOfDeath.deathMessage}".miniMsg()))
    }

    @EventHandler
    fun FoodLevelChangeEvent.onFoodChange() {
        if ((entity as? Player)?.isInHub() == true) isCancelled = true
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    fun PlayerCreateGraveEvent.onCreateGrave() {
//        if (player.isInHub()) isCancelled = true
//    }
}
