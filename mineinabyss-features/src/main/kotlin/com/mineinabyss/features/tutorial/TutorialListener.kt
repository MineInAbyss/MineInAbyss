package com.mineinabyss.features.tutorial

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

class TutorialListener : Listener {

    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() {
        tutorial.tutorialEntities.filter { it.location.chunk == chunk }.forEach(TutorialEntity::spawn)
    }
}