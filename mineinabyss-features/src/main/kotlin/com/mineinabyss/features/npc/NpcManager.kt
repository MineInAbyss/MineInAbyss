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
    // probably not needed
    val npcEntities = mutableListOf<NpcEntity>()
    val npcMap: MutableMap<Long, List<NpcEntity>> = mutableMapOf()

    fun initNpc() {
        // load npc config
        for (npc in npcsConfig.npcs.values) {
            val npcEntity = NpcEntity(npc, world, dialogsConfig)

            npcEntities += npcEntity
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
//        val previous = entity.lastInteraction
//        val timeprev = previous?.timestamp
//        val prevplayer = previous?.player
//        val now = System.currentTimeMillis()
//        val five_sec = 5000L
//        if (prevplayer == player && timeprev != null && now - timeprev < five_sec) {
//            player.error("Please wait before interacting again.")
//            this.isCancelled = true
//            return
//        }
        val dialogData = gearyEntity.get<DialogData>()
        val dialogId = npcData.dialogId
        val questDialogData = gearyEntity.get<QuestDialogData>()
        when {
            dialogData == null -> player.sendMessage("dialog data missing for npc ${npcData.id}")
            dialogId != null && questDialogData != null -> npcData.defaultInteraction(player, dialogId, dialogData, questDialogData)
            dialogId != null -> {
                player.sendMessage("This NPC is missing dialog data for ID: $dialogId")
                npcData.fallbackInteraction(player)
            }
        }
    }
}


