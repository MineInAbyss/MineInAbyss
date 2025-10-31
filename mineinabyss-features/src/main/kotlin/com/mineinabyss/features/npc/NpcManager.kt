package com.mineinabyss.features.npc

import com.mineinabyss.features.npc.action.DialogData
import com.mineinabyss.features.npc.action.DialogsConfig
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import org.bukkit.World
import org.bukkit.entity.Interaction
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.world.ChunkLoadEvent


// how to use:
// load npcs config from config
// load list of all dialogs ids
// create NpcManager with npcs config, world, and dialogs ids
// init it
// register listeners
class NpcManager(
    val npcsConfig: NpcsConfig,
    val world: World,
    val dialogsConfig: DialogsConfig,
): Listener {
    // probably not needed
    var npcEntities: List<NpcEntity> = emptyList()
    val npcMap: MutableMap<Long, List<NpcEntity>> = mutableMapOf()

//    val api = LuxDialoguesAPI.getAPI().getProvider()
    fun initNpc() {
        // load npc config
        for (npc in npcsConfig.npcs.values) {
            val npcEntity = NpcEntity(npc, world, dialogsConfig)
            npcEntities = npcEntities + npcEntity

            val chunkKey = npc.location.chunk.chunkKey
            npcMap[chunkKey] = npcMap.getOrDefault(chunkKey, emptyList()) + npcEntity
            println("Loaded NPC ${npc.id} at chunk $chunkKey")
        }
    println("NPC Manager initialized with ${npcEntities.size} NPCs.")
    println("npc values are ${npcsConfig.npcs.values}")
    }

    @EventHandler
    fun ChunkLoadEvent.handleNpcSpawn() {
        //spawn npc
        npcMap[chunk.chunkKey]?.forEach(NpcEntity::createBaseNpc)
    }


    @EventHandler
    fun PlayerInteractEntityEvent.handleNpcInteraction() {
        val player = this.player
        val entity = this.rightClicked
        val gearyEntity = entity.toGearyOrNull() ?: return
        val NpcData = gearyEntity.get<Npc>() ?: return
        val dialogId: String? = NpcData.dialogId
        if (entity !is Interaction) {
            return
        }
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
        if (dialogData == null) {
            player.sendMessage("dialog data missing for npc ${NpcData.id}")
        }
        if (dialogId != null && dialogData != null) {
            NpcData.defaultInteraction(player, dialogId, dialogData)
        } else {
            if (dialogId != null) {
                player.sendMessage("This NPC is missing dialog data for ID: $dialogId")
                NpcData.fallbackInteraction(player)
            }
        }
    }
}


