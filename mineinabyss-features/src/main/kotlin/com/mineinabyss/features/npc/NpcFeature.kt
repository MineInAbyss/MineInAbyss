package com.mineinabyss.features.npc

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.loading.KeyedRegistry
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.features.locations.LocationPoints
import com.mineinabyss.features.locations.LocationPointsFeature
import com.mineinabyss.features.npc.TradeTables
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.info
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.generator.ModelGenerator
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.io.path.div
import kotlin.time.Duration.Companion.minutes

private val MODEL_IMPORT_TIMEOUT = 2.minutes

val npcSpawner: NpcSpawner? get() = loaded(NpcFeature)
val tradeTables: TradeTables? get() = loaded(NpcFeature)

val NpcFeature = module("npcs") {
    require(get<AbyssFeatureConfig>().npcs.enabled) { "Npcs feature is disabled" }
    val points = singleModule(LocationPointsFeature).get<LocationPoints>()

    val npcsPath = plugin.dataPath / "npcs"
    val trades by single<TradeTables> {
        KeyedRegistry(npcsPath / NpcSpawner.TRADES_FOLDER, "trade table", "trade tables", TradeTable.serializer(), TradeTable::itemErrors)
    }
    val spawner by single { NpcSpawner(npcsPath, trades) }

    listeners(spawner, NpcListener(spawner))
    val modelListener = if (abyss.isModelEngineEnabled) NpcModelListener(spawner).also { listeners(it) } else null
    addCloseable { spawner.despawnAll() }
    // onInteract decodes Geary actions, whose serializers are only complete once Geary has loaded its prefabs
    launch {
        PrefabLoading.awaitLoaded().await()
        // Dialogs NPCs open are checked against the loaded dialogs, location points must exist for NPCs and their actions to decode
        dialogSessions?.loaded?.await()
        points.loaded.await()
        // ModelEngine queues tasks without locking, so one added while it runs the phase can be missed
        if (modelListener != null && withTimeoutOrNull(MODEL_IMPORT_TIMEOUT) { awaitModelsImported(modelListener) } == null)
            abyss.logger.w("ModelEngine has not finished importing models, loading NPCs anyway")
        trades.reload()
        spawner.reload()
    }
}.mainCommand {
    "npcs" {
        permission = "mineinabyss.npcs"

        "reload" {
            executes {
                val errors = get<TradeTables>().reload() + get<NpcSpawner>().reload()
                sender.reportReload("npcs", get<NpcSpawner>().npcs.size, errors.size)
            }
        }
        "list" {
            executes {
                get<NpcSpawner>().npcs.forEach { (key, npc) ->
                    sender.info("$key <gray>${npc.location.blockX} ${npc.location.blockY} ${npc.location.blockZ}")
                }
            }
        }
        "tp" {
            executes.asPlayer().args("npc" to Args.prefabKeyIn { get<NpcSpawner>().npcs.keys }) { key ->
                player.teleportAsync(get<NpcSpawner>().npcs.getValue(key).location)
            }
        }
    }
    "trades" {
        permission = "mineinabyss.trades"

        "open" {
            executes.asPlayer().args("table" to Args.prefabKeyIn { get<TradeTables>().entries.keys }) { key ->
                get<TradeTables>().entries.getValue(key).open(player)
            }
        }
    }
}

private suspend fun awaitModelsImported(listener: NpcModelListener) = suspendCancellableCoroutine { continuation ->
    val caller = Thread.currentThread()
    // ModelEngine runs the task in place when the import already finished, and that import's event has fired by then
    ModelEngineAPI.getAPI().modelGenerator.queueTask(ModelGenerator.Phase.POST_IMPORT) {
        if (Thread.currentThread() != caller && continuation.isActive) listener.skipNextImport()
        continuation.resume(Unit)
    }
}
