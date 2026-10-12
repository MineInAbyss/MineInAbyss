package com.mineinabyss.features.npc

import com.mineinabyss.features.abyss
import com.ticxo.modelengine.api.events.ModelRegistrationEvent
import com.ticxo.modelengine.api.generator.ModelGenerator
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.atomic.AtomicBoolean

class NpcModelListener(private val spawner: NpcSpawner) : Listener {
    private val skipArmed = AtomicBoolean(false)

    // ModelEngine fires the event right after running the POST_IMPORT tasks, on the same thread.
    // NpcFeature calls this from such a task when it reloads npcs itself for that import
    fun skipNextImport() = skipArmed.set(true)

    // ModelEngine imports models and fires this off the main thread
    // The first import at startup is left to NpcFeature, which also waits for Geary prefabs
    @EventHandler
    fun ModelRegistrationEvent.onModelsImported() {
        // An import with no models skips the POST_IMPORT event, so a skip for it must not carry over to the next import
        if (phase == ModelGenerator.Phase.FINISHED) skipArmed.set(false)
        if (phase != ModelGenerator.Phase.POST_IMPORT) return
        if (skipArmed.getAndSet(false) || !spawner.loaded) return
        Bukkit.getScheduler().runTask(abyss, Runnable { spawner.reload() })
    }
}
