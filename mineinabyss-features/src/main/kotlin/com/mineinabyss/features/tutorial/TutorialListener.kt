package com.mineinabyss.features.tutorial

import com.mineinabyss.deeperworld.world.CubePoint
import com.mineinabyss.features.hubstorage.isInHub
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.ChunkLoadEvent

class TutorialListener : Listener {

    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() {
        tutorial.tutorialEntities[chunk.chunkKey]?.forEach(TutorialEntity::spawn)
    }

    @EventHandler
    fun PlayerMoveEvent.onTutorial() {
        val cubePoint = player.takeIf { hasExplicitlyChangedBlock() && player.isInHub() }?.location?.let { CubePoint(it.blockX, it.blockY, it.blockZ) } ?: return
        when (cubePoint) {
            in tutorial.entry.region -> player.teleport(tutorial.entry.target.toLocation(player.world))
            in tutorial.exit.region -> player.teleport(tutorial.exit.target.toLocation(player.world))
        }
    }
}