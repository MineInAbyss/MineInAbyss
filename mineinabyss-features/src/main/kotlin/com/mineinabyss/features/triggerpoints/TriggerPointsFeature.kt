package com.mineinabyss.features.triggerpoints

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.dialogs.dialogSessions
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.features.locations.LocationPoints
import com.mineinabyss.features.locations.LocationPointsFeature
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.Plugins
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.io.path.div
import kotlin.time.Duration.Companion.seconds

private val TRIGGER_REGISTER_TIMEOUT = 30.seconds

@Serializable
data class TriggerPointsConfig(
    /** Nexo block placed on every block of a BLOCK trigger point */
    val triggerBlock: PrefabKey = PrefabKey.of("mineinabyss", "gondola_trigger"),
)

val TriggerPointsFeature = module("triggerPoints") {
    require(get<AbyssFeatureConfig>().triggerPoints.enabled) { "Trigger points feature is disabled" }
    val points = singleModule(LocationPointsFeature).get<LocationPoints>()

    val config by singleConfig<TriggerPointsConfig>("triggerPoints/config.yml") { default = TriggerPointsConfig() }
    val pointsPath = plugin.dataPath / "triggerPoints"
    val registry by single { TriggerPointRegistry(pointsPath) }
    val triggerBlocks = if (Plugins.isEnabled("Nexo")) TriggerBlocks(config) else null
    single { TriggerPointReloader(registry, triggerBlocks) }

    listeners(TriggerPointListener(registry, triggerBlocks))
    if (triggerBlocks != null) listeners(triggerBlocks)

    // Actions are Geary actions, and trigger blocks are Geary prefabs that geary-nexo registers with Nexo some time after prefabs have loaded
    launch {
        PrefabLoading.awaitLoaded().await()
        // Dialogs and location points trigger points use are checked against the loaded ones
        dialogSessions?.loaded?.await()
        points.loaded.await()
        registry.reload()
        if (triggerBlocks == null) return@launch

        triggerBlocks.rebuild(registry.blocks)
        repeat(TRIGGER_REGISTER_TIMEOUT.inWholeSeconds.toInt()) {
            if (triggerBlocks.isRegistered()) return@launch triggerBlocks.placeInLoadedChunks()
            delay(1.seconds)
        }
        abyss.logger.w("Trigger block ${config.triggerBlock} is not a Nexo custom block, check its prefab has nexo:custom_block")
    }
}.mainCommand {
    "triggerpoints" {
        permission = "mineinabyss.triggerpoints"

        "reload" {
            description = "Reloads trigger point files, not config.yml"
            executes {
                val errors = get<TriggerPointReloader>().reload()
                sender.reportReload("trigger points", get<TriggerPointRegistry>().points.size, errors.size)
            }
        }
        "list" {
            executes {
                get<TriggerPointRegistry>().points.forEach { (key, point) ->
                    val where = when (point) {
                        is TriggerBlock -> "block at ${point.location}"
                        is TriggerBox -> "box from ${point.pointA} to ${point.pointB}"
                    }
                    sender.info("$key <gray>$where")
                }
            }
        }
        "tp" {
            executes.asPlayer().args("point" to Args.prefabKeyIn { get<TriggerPointRegistry>().points.keys }) { key ->
                val registry = get<TriggerPointRegistry>()
                val location = when (val point = registry.points.getValue(key)) {
                    is TriggerBlock -> point.location.resolve().toBlockLocation().add(0.5, 0.0, 0.5)
                    is TriggerBox -> registry.boxes.values.flatten().first { it.key == key }
                        .let { Location(it.world, it.bounds.centerX, it.bounds.minY, it.bounds.centerZ) }
                }
                player.teleportAsync(location)
            }
        }
    }
}

class TriggerPointReloader(private val registry: TriggerPointRegistry, private val triggerBlocks: TriggerBlocks?) {
    fun reload(): List<String> {
        val errors = registry.reload()
        triggerBlocks?.rebuild(registry.blocks)
        triggerBlocks?.placeInLoadedChunks()
        return errors
    }
}
