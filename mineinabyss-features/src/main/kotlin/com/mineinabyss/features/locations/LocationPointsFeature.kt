package com.mineinabyss.features.locations

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.helpers.loaded
import com.mineinabyss.features.helpers.prefabKeyIn
import com.mineinabyss.features.helpers.reportReload
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.features.*
import com.mineinabyss.idofront.messaging.info
import kotlinx.coroutines.future.await
import kotlin.io.path.div

val locationPoints: LocationPoints? get() = loaded(LocationPointsFeature)

val LocationPointsFeature = module("locationPoints") {
    require(get<AbyssFeatureConfig>().locationPoints.enabled) { "Location points feature is disabled" }

    val pointsPath = plugin.dataPath / "locationPoints.yml"
    val points by single { LocationPoints(pointsPath) }

    // Waits like the features using these points, so worlds loaded by other plugins exist by then
    launch {
        PrefabLoading.awaitLoaded().await()
        points.reload()
    }
}.mainCommand {
    "locationpoints" {
        permission = "mineinabyss.locationpoints"

        "reload" {
            executes {
                val errors = get<LocationPoints>().reload()
                sender.reportReload("location points", get<LocationPoints>().keys.size, errors.size)
            }
        }
        "list" {
            executes {
                val points = get<LocationPoints>()
                points.keys.sortedBy { it.full }.forEach { key ->
                    val location = points[key]!!
                    sender.info("$key <gray>${location.x} ${location.y} ${location.z} ${location.world.name}")
                }
            }
        }
        "tp" {
            executes.asPlayer().args("point" to Args.prefabKeyIn { get<LocationPoints>().keys }) { key ->
                player.teleportAsync(get<LocationPoints>()[key]!!)
            }
        }
    }
}
