package com.mineinabyss.features.npc

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.actions.brokenDialogs
import com.mineinabyss.features.helpers.ChunkPos
import com.mineinabyss.features.helpers.actionsOf
import com.mineinabyss.features.helpers.chunkPos
import com.mineinabyss.features.helpers.loading.KeyedYamlLoader
import com.mineinabyss.features.helpers.pos
import com.mineinabyss.features.locations.ResolvedLocationPointSerializer
import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.serialization.VectorAltSerializer
import com.mineinabyss.idofront.spawning.spawn
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.toPlainText
import com.mineinabyss.idofront.time.ticks
import com.ticxo.modelengine.api.ModelEngineAPI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.util.Vector
import org.joml.Vector3f
import java.nio.file.Path

private const val NPC_TAG = "mineinabyss_npc"
private const val NPC_KEY_TAG_PREFIX = "mineinabyss_npc="
// Degrees an npc turns each tick while it turns towards a player or back
private const val TURN_PER_TICK = 12f
// An NPC further away than this is serving somewhere else
private const val NEARBY_RADIUS = 16.0

val Entity.npcKey: PrefabKey?
    get() = scoreboardTags.firstOrNull { it.startsWith(NPC_KEY_TAG_PREFIX) }?.let { PrefabKey.ofOrNull(it.removePrefix(NPC_KEY_TAG_PREFIX)) }

class NpcSpawner(private val root: Path, private val trades: TradeTables) : Listener {
    val npcs: Map<PrefabKey, NpcDefinition> field = mutableMapOf()
    private var byChunk: Map<ChunkPos, List<PrefabKey>> = emptyMap()
    @Volatile
    var loaded = false
        private set

    fun reload(): List<String> = try {
        val files = KeyedYamlLoader.load(root, excludedNamespaces = setOf(TRADES_FOLDER))
        npcs.clear()
        npcs += files.decodeAll("npc", NpcDefinition.serializer())
        files.errors += checkDialogs()
        files.errors += checkTrades()
        files.errors += checkModels()
        byChunk = npcs.entries
            .filter { it.value.location.isWorldLoaded }
            .groupBy({ it.value.location.chunkPos }, { it.key })

        respawn()
        files.logErrors(npcs.size, "npcs")
    } finally {
        loaded = true
    }

    private fun checkDialogs(): List<String> = npcs.flatMap { (key, npc) ->
        npc.onInteract.brokenDialogs().map { "Npc $key opens a broken dialog: $it" }
    }

    private fun checkTrades(): List<String> = npcs.flatMap { (key, npc) ->
        npc.onInteract.actionsOf<OpenTradesAction>().map { it.table }.mapNotNull { source ->
            val table = source.resolveWith(trades::get) ?: return@mapNotNull "Npc $key opens trade table $source which does not exist"
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

    fun entity(key: PrefabKey): Entity? =
        npcs[key]?.location?.takeIf { it.isWorldLoaded && it.isChunkLoaded }?.chunk?.entities?.firstOrNull { it.npcKey == key && it.isValid }

    fun nearest(location: Location): Map.Entry<PrefabKey, NpcDefinition>? = npcs.entries
        .filter { it.value.location.world == location.world }
        .minByOrNull { it.value.location.distanceSquared(location) }
        ?.takeIf { it.value.location.distanceSquared(location) <= NEARBY_RADIUS * NEARBY_RADIUS }

    private val turning = mutableMapOf<PrefabKey, Job>()

    /** Keeps the npc turning towards [player] until [faceHome] */
    fun face(key: PrefabKey, player: Player) = turn(key) { player.location }

    fun faceHome(key: PrefabKey) = turn(key, null)

    private fun turn(key: PrefabKey, target: (() -> Location)?) {
        turning.remove(key)?.cancel()
        val entity = entity(key) ?: return
        val home = npcs[key]?.location?.yaw ?: return
        turning[key] = abyss.launch {
            while (entity.isValid) {
                val current = entity.location
                val wanted = target?.invoke()?.let { current.clone().setDirection(it.toVector().subtract(current.toVector())).yaw } ?: home
                val diff = Location.normalizeYaw(wanted - current.yaw)
                if (target == null && diff == 0f) break
                if (diff != 0f) entity.setRotation(current.yaw + diff.coerceIn(-TURN_PER_TICK, TURN_PER_TICK), current.pitch)
                delay(1.ticks)
            }
        }
    }

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
        val keys = byChunk[chunk.pos] ?: return
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

    companion object {
        const val TRADES_FOLDER = "trades"
    }
}

const val NPC_KEY = "npc"
const val NPC_ENTITY_KEY = "npc_entity"

@Serializable
class NpcDefinition(
    val name: String,
    val location: @Serializable(ResolvedLocationPointSerializer::class) Location,
    val scale: @Serializable(VectorAltSerializer::class) Vector = Vector(1, 1, 1),
    val model: String? = null,
    val showName: Boolean = true,
    val speaker: String? = null,
    val dialogStyle: String? = null,
    val onInteract: Tasks? = null,
) {
    val speakerName: String
        get() = speaker ?: name.miniMsg().toPlainText()
}
