package com.mineinabyss.features.npc

import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.actions.OpenDialogAction
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.features.helpers.loading.gearyYaml
import com.mineinabyss.features.npc.trades.OpenTradesAction
import com.mineinabyss.features.npc.trades.TradeTables
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.ticxo.modelengine.api.ModelEngineAPI
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.joml.Vector3f
import java.nio.file.Path
import java.util.*

private const val NPC_TAG = "mineinabyss_npc"
private const val NPC_KEY_TAG_PREFIX = "mineinabyss_npc="
// An NPC further away than this is serving somewhere else
private const val NEARBY_RADIUS = 16.0

val Entity.npcKey: PrefabKey?
    get() = scoreboardTags.firstOrNull { it.startsWith(NPC_KEY_TAG_PREFIX) }?.let { PrefabKey.ofOrNull(it.removePrefix(NPC_KEY_TAG_PREFIX)) }

class NpcSpawner(private val root: Path, private val trades: TradeTables) : Listener {
    private val yaml = gearyYaml()

    val npcs: Map<PrefabKey, NpcDefinition> field = mutableMapOf()
    private var byChunk: Map<ChunkPos, List<PrefabKey>> = emptyMap()
    @Volatile
    var loaded = false
        private set

    private data class ChunkPos(val world: UUID, val key: Long)

    fun reload(): List<String> = try {
        val files = KeyedYamlLoader.load(root, excludedNamespaces = setOf(TRADES_FOLDER))
        npcs.clear()
        npcs += files.decodeAll("npc", NpcDefinition.serializer(), yaml)
        files.errors += checkDialogs()
        files.errors += checkTrades()
        files.errors += checkModels()
        byChunk = npcs.entries
            .filter { it.value.location.isWorldLoaded }
            .groupBy({ it.value.chunkPos() }, { it.key })

        respawn()
        files.logErrors(npcs.size, "npcs")
    } finally {
        loaded = true
    }

    // Dialogs written in place are only decoded when opened, so check them here to report problems on load
    private fun checkDialogs(): List<String> {
        val registry = dialogSessions?.registry ?: return emptyList()
        return npcs.flatMap { (key, npc) ->
            npc.actionsOf<OpenDialogAction>().mapNotNull { action ->
                runCatching { action.resolve(registry) }.exceptionOrNull()?.let { "Npc $key opens a broken dialog: ${it.message}" }
            }
        }
    }

    private fun checkTrades(): List<String> = npcs.flatMap { (key, npc) ->
        npc.actionsOf<OpenTradesAction>().map { it.table }.mapNotNull { source ->
            val table = source.resolve(trades) ?: return@mapNotNull "Npc $key opens trade table $source which does not exist"
            table.itemErrors()?.let { "Npc $key opens trade table $source which $it" }
        }
    }

    private fun checkModels(): List<String> {
        if (npcs.isEmpty()) return emptyList()
        if (!abyss.isModelEngineEnabled) {
            abyss.logger.w("ModelEngine is not enabled, so npcs will be invisible and cannot be clicked")
            return emptyList()
        }

        return npcs.mapNotNull { (key, npc) ->
            when {
                npc.model == null -> "Npc $key has no model, so it will be invisible and cannot be clicked"
                ModelEngineAPI.getBlueprint(npc.model) == null -> "Npc $key uses unknown model ${npc.model}"
                else -> null
            }
        }
    }

    private inline fun <reified T> NpcDefinition.actionsOf(): List<T> = onInteract?.tasks.orEmpty().mapNotNull { it.action as? T }

    fun entity(key: PrefabKey): Entity? =
        npcs[key]?.location?.takeIf { it.isWorldLoaded && it.isChunkLoaded }?.chunk?.entities?.firstOrNull { it.npcKey == key }

    fun nearest(location: Location, radius: Double = NEARBY_RADIUS, filter: (NpcDefinition) -> Boolean = { true }): Map.Entry<PrefabKey, NpcDefinition>? =
        npcs.entries
            .filter { (_, npc) -> npc.location.world == location.world && filter(npc) }
            .associateWith { it.value.location.distanceSquared(location) }
            .filterValues { it <= radius * radius }
            .minByOrNull { it.value }?.key

    private fun respawn() {
        despawnAll()
        Bukkit.getWorlds().forEach { world -> world.loadedChunks.forEach(::spawnIn) }
    }

    fun despawnAll() = Bukkit.getWorlds().forEach { world ->
        world.getEntitiesByClass(ItemDisplay::class.java).filter { NPC_TAG in it.scoreboardTags }.forEach(::despawn)
    }

    private fun despawn(entity: Entity) {
        if (abyss.isModelEngineEnabled) ModelEngineAPI.removeModeledEntity(entity)
        entity.remove()
    }

    @EventHandler
    fun ChunkLoadEvent.onChunkLoad() = spawnIn(chunk)

    private fun spawnIn(chunk: Chunk) {
        val keys = byChunk[ChunkPos(chunk.world.uid, chunk.chunkKey)] ?: return
        chunk.entities.filter { NPC_TAG in it.scoreboardTags }.forEach(::despawn)
        keys.forEach { key -> npcs[key]?.let { spawn(key, it) } }
    }

    private fun spawn(key: PrefabKey, npc: NpcDefinition) {
        val entity = npc.location.spawn<ItemDisplay> {
            addScoreboardTag(NPC_TAG)
            addScoreboardTag(NPC_KEY_TAG_PREFIX + key.full)
            customName(npc.name.miniMsg())
            isCustomNameVisible = npc.showName
            isPersistent = false
        }

        if (npc.model != null && abyss.isModelEngineEnabled) {
            val blueprint = ModelEngineAPI.getBlueprint(npc.model) ?: return
            val model = ModelEngineAPI.createActiveModel(blueprint)
            model.scale = Vector3f(npc.scale.x.toFloat(), npc.scale.y.toFloat(), npc.scale.z.toFloat())
            ModelEngineAPI.createModeledEntity(entity).addModel(model, true)
        }
    }

    private fun NpcDefinition.chunkPos() =
        ChunkPos(location.world.uid, Chunk.getChunkKey(location.blockX shr 4, location.blockZ shr 4))

    companion object {
        const val TRADES_FOLDER = "trades"
    }
}
