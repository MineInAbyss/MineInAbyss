package com.mineinabyss.features.npc

import com.mineinabyss.features.cinematics.isInCinematic
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.dialogs.runtime.SPEAKER_KEY
import com.mineinabyss.features.dialogs.runtime.STYLE_KEY
import com.mineinabyss.features.dialogs.runtime.dialogContext
import com.mineinabyss.features.dialogs.runtime.runIn
import com.mineinabyss.features.warps.isWarping
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot

const val NPC_KEY = "npc"
const val NPC_ENTITY_KEY = "npc_entity"

class NpcListener(private val spawner: NpcSpawner) : Listener {
    @EventHandler
    fun PlayerInteractEntityEvent.onInteract() {
        val key = rightClicked.npcKey ?: return
        isCancelled = true

        if (hand != EquipmentSlot.HAND) return
        if (dialogSessions?.isInDialog(player) == true) return
        if (player.isInCinematic() || player.isWarping()) return

        val npc = spawner.npcs[key] ?: return
        val context = dialogContext(
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
