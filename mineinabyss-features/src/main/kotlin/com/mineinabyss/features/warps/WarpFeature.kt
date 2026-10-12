package com.mineinabyss.features.warps

import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.prefabKey
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.default
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.plugin.Plugins
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.bukkit.entity.Player
import kotlin.io.path.div
import kotlin.time.Duration.Companion.seconds

private val TRIGGER_REGISTER_TIMEOUT = 30.seconds

fun Player.isWarping() = loaded<Warps>(WarpFeature)?.isWarping(this) == true

val WarpFeature = module("warps") {
    require(get<AbyssFeatureConfig>().warps.enabled) { "Warps feature is disabled" }

    val warpsPath = plugin.dataPath / "warps"
    val config by singleConfig<WarpsConfig>("warps/config.yml") { default = WarpsConfig() }
    val warps by single { Warps(config) }
    val registry by single { WarpRegistry(warpsPath, config) }
    val travel by single { WarpTravel(config, registry, warps) }
    val triggerBlocks = if (Plugins.isEnabled("Nexo")) WarpTriggerBlocks(config) else null
    single { WarpReloader(registry, triggerBlocks) }

    listeners(warps)
    if (triggerBlocks != null) listeners(triggerBlocks, WarpTriggerListener(travel, warps, triggerBlocks))
    addCloseable { warps.cancelAll() }

    // Route conditions are Geary conditions, and trigger blocks are Geary prefabs that geary-nexo registers with Nexo some time after prefabs have loaded
    launch {
        PrefabLoading.awaitLoaded().await()
        registry.reload()
        if (triggerBlocks == null) return@launch

        triggerBlocks.rebuild(registry.warps)
        repeat(TRIGGER_REGISTER_TIMEOUT.inWholeSeconds.toInt()) {
            if (triggerBlocks.isRegistered()) return@launch triggerBlocks.placeInLoadedChunks()
            delay(1.seconds)
        }
        abyss.logger.w("Warp trigger block ${config.triggerBlock} is not a Nexo custom block, check its prefab has nexo:custom_block")
    }
}.mainCommand {
    "warps" {
        permission = "mineinabyss.warps"

        "reload" {
            description = "Reloads warp files, not config.yml"
            executes {
                val errors = get<WarpReloader>().reload()
                sender.reportReload("warps", get<WarpRegistry>().warps.size, errors.size)
            }
        }
        "list" {
            executes {
                get<WarpRegistry>().warps.forEach { (key, warp) -> sender.info("$key <gray>${warp.name}, stops: ${warp.stops.keys.joinToString()}") }
            }
        }
        "travel" {
            description = "Travels a warp from the stop closest to you"
            executes.asPlayer().args(
                "warp" to Args.prefabKey().oneOf { get<WarpRegistry>().warps.keys.toList() },
                "to" to Args.string().default { "" },
            ) { key, to ->
                if (get<WarpRegistry>()[key] == null) return@args player.error("Warp $key not found")
                get<WarpTravel>().travel(player, key, to.ifEmpty { null })
            }
        }
    }
}

class WarpReloader(private val registry: WarpRegistry, private val triggerBlocks: WarpTriggerBlocks?) {
    fun reload(): List<String> {
        val errors = registry.reload()
        triggerBlocks?.rebuild(registry.warps)
        triggerBlocks?.placeInLoadedChunks()
        return errors
    }
}
