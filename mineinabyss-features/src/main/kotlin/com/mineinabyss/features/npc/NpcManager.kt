package com.mineinabyss.features.npc

import com.mineinabyss.features.npc.shopkeeping.listenerSingleton
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
    var npcEntities: List<NpcEntity> = emptyList()
    val npcMap: MutableMap<Long, List<NpcEntity>> = mutableMapOf()

    fun initNpc() {
        // load npc config
        for (npc in npcsConfig.npcs.values) {
            val npcEntity = NpcEntity(npc, world, dialogsConfig)
            npcEntities = npcEntities + npcEntity

            val chunkKey = npc.location.chunk.chunkKey
            npcMap[chunkKey] = npcMap.getOrDefault(chunkKey, emptyList()) + npcEntity
            println("Loaded NPC ${npc.id} at chunk ${npc.location}")

            if (npc.location.isWorldLoaded && npc.location.isChunkLoaded) npcEntity.createBaseNpc()

        }
        println("NPC Manager initialized with ${npcEntities.size} NPCs.")
        println("npc values are ${npcsConfig.npcs.values}")
        listenerSingleton.bstgth = npcMap
    }

    @EventHandler
    fun ChunkLoadEvent.handleNpcSpawn() {
        listenerSingleton.bstgth[chunk.chunkKey]?.forEach(NpcEntity::createBaseNpc)
    }


    @EventHandler
    fun PlayerInteractEntityEvent.handleNpcInteraction() {
        val player = this.player
        val entity = this.rightClicked
        val gearyEntity = entity.toGearyOrNull() ?: return
        val NpcData = gearyEntity.get<Npc>() ?: return
        val dialogId: String? = NpcData.dialogId
        if (entity !is ItemDisplay) return

        val dialogData = gearyEntity.get<DialogData>()
        val questDialogData = gearyEntity.get<QuestDialogData>()
        if (dialogData == null) {
            player.sendMessage("dialog data missing for npc ${NpcData.id}")
        }
        if (dialogId != null && dialogData != null && questDialogData != null) {
            NpcData.defaultInteraction(player, dialogId, dialogData, questDialogData)
        } else {
            if (dialogId != null) {
                player.sendMessage("This NPC is missing dialog data for ID: $dialogId")
                NpcData.fallbackInteraction(player)
            }
        }
    }
}


