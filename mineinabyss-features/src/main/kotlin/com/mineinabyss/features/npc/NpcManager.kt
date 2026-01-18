package com.mineinabyss.features.npc

import com.mineinabyss.features.npc.action.DialogData
import com.mineinabyss.features.npc.action.DialogsConfig
import com.mineinabyss.features.npc.action.QuestDialogData
import com.mineinabyss.features.npc.shopkeeping.ListenerSingleton
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class NpcManager(
    val npcsConfig: NpcsConfig,
    val world: World,
    val dialogsConfig: DialogsConfig,
): Listener {
    val npcMap: MutableMap<Long, List<NpcEntity>> = mutableMapOf()

    fun initNpc() {
        // load npc config
        for (npc in npcsConfig.npcs.values) {
            val npcEntity = NpcEntity(npc, world, dialogsConfig)

            npcMap.compute(npc.location.chunk.chunkKey) { _, list -> (list ?: listOf()).plus(npcEntity) }
            if (npc.location.isWorldLoaded && npc.location.isChunkLoaded) npcEntity.createBaseNpc()

        }
        ListenerSingleton.bstgth = npcMap
    }

    @EventHandler
    fun ChunkLoadEvent.handleNpcSpawn() {
        ListenerSingleton.bstgth[chunk.chunkKey]?.forEach(NpcEntity::createBaseNpc)
    }


    @EventHandler
    fun PlayerInteractEntityEvent.handleNpcInteraction() {
        val entity = rightClicked as? ItemDisplay ?: return
        val gearyEntity = entity.toGearyOrNull() ?: return
        val npcData = gearyEntity.get<Npc>() ?: return

        val dialogData = gearyEntity.get<DialogData>()
        val questDialogData = gearyEntity.get<QuestDialogData>()
        when {
            npcData.dialogId == null -> return
            dialogData != null && questDialogData != null ->
                npcData.defaultInteraction(player, npcData.dialogId, dialogData, questDialogData)
            else -> when (dialogData) {
                null -> player.sendMessage("dialog data missing for npc ${npcData.id}")
                else -> player.sendMessage("This NPC is missing dialog data for ID: ${npcData.dialogId}")
            }.apply { npcData.fallbackInteraction(player) }
        }
    }
}


