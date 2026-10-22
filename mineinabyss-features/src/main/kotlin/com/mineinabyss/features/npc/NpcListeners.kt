package com.mineinabyss.features.npc

import com.mineinabyss.features.dialogs.runtime.SPEAKER_KEY
import com.mineinabyss.features.dialogs.runtime.STYLE_KEY
import com.mineinabyss.features.helpers.actionContext
import com.mineinabyss.features.helpers.isInCinematicOrTransition
import com.mineinabyss.features.helpers.isInDialog
import com.mineinabyss.features.helpers.nextTick
import com.mineinabyss.features.helpers.runIn
import com.ticxo.modelengine.api.events.ModelRegistrationEvent
import com.ticxo.modelengine.api.generator.ModelGenerator
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.concurrent.atomic.AtomicBoolean

class NpcListener(private val spawner: NpcSpawner) : Listener {
    @EventHandler
    fun PlayerInteractEntityEvent.onInteract() {
        val key = rightClicked.npcKey ?: return
        isCancelled = true

        if (hand != EquipmentSlot.HAND) return
        if (player.isInDialog()) return
        if (player.isInCinematicOrTransition()) return

        val npc = spawner.npcs[key] ?: return
        val context = actionContext(
            player, mapOf(
                NPC_KEY to npc,
                NPC_ENTITY_KEY to rightClicked,
                SPEAKER_KEY to npc.speakerName,
                STYLE_KEY to npc.dialogStyle,
            )
        )
        npc.onInteract?.runIn(context)
    }
}

class NpcModelListener(private val spawner: NpcSpawner) : Listener {
    private val skipArmed = AtomicBoolean(false)

    // ModelEngine fires the event right after running the POST_IMPORT tasks, on the same thread.
    // NpcFeature calls this from such a task when it reloads npcs itself for that import
    fun skipNextImport() = skipArmed.set(true)

    // ModelEngine imports models and fires this off the main thread.
    // The first import at startup is left to NpcFeature, which also waits for Geary prefabs
    @EventHandler
    fun ModelRegistrationEvent.onModelsImported() {
        // An import with no models skips the POST_IMPORT event, so a skip for it must not carry over to the next import
        if (phase == ModelGenerator.Phase.FINISHED) skipArmed.set(false)
        if (phase != ModelGenerator.Phase.POST_IMPORT) return
        if (skipArmed.getAndSet(false) || !spawner.loaded) return
        nextTick { spawner.reload() }
    }
}
