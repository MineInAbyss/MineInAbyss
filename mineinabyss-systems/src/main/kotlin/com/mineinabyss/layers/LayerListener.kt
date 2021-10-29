package com.mineinabyss.layers

import com.derongan.minecraft.deeperworld.event.PlayerAscendEvent
import com.derongan.minecraft.deeperworld.event.PlayerChangeSectionEvent
import com.derongan.minecraft.deeperworld.event.PlayerDescendEvent
import com.derongan.minecraft.deeperworld.services.PlayerManager
import com.derongan.minecraft.deeperworld.world.section.section
import com.mineinabyss.mineinabyss.core.layer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class LayerListener : Listener {
    @EventHandler
    private fun PlayerAscendEvent.onPlayerAscend() = sendTitleOnLayerChange()

    @EventHandler(ignoreCancelled = true)
    private fun PlayerDescendEvent.onPlayerDescend() = sendTitleOnLayerChange()

    private fun PlayerChangeSectionEvent.sendTitleOnLayerChange() {
        val player = player
        if (PlayerManager.playerCanTeleport(player)) {
            val fromSection = fromSection
            val toSection = toSection
            val fromLayer = fromSection.layer ?: return
            val toLayer = toSection.layer ?: return

            if (fromLayer != toLayer) {
                player.sendTitle(toLayer.name, toLayer.sub, 50, 10, 20)
            }
        }
    }

    @EventHandler
    fun PlayerDeathEvent.appendLayerToDeathMessage() {
        val player = entity
        val section = player.location.section ?: return
        val layerOfDeath = section.layer ?: return
        apply {
            deathMessage += " ${layerOfDeath.deathMessage}"
        }
    }
}
