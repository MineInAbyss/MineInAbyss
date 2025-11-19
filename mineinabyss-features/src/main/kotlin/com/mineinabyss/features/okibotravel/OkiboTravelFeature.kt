package com.mineinabyss.features.okibotravel

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf

val OkiboTravelFeature: Feature = feature("okibo-travel") {
    dependsOn {
        plugins("BKCommonLib", "Train_Carts", "TCCoasters")
    }

    scopedModule {
        scoped<OkiboTravelConfig> {
            config("okiboTravel", abyss.dataPath, OkiboTravelConfig(), onReload = {
                removeOkiboMaps()

                mapEntities.clear()
                hitboxEntities.clear()
                hitboxIconEntities.clear()

                spawnOkiboMaps()
            }, onLoad = { config ->
                abyss.logger.s("Reloaded OkiboLines")
            }).getOrLoad()
        }

        scopedOf(::OkiboTravelListener)
    }

    onEnable {
        listeners(get<OkiboTravelListener>())
    }

    mainCommand {
        "okibo" {
            "reload" {
                executes {
                    featureManager.reload(OkiboTravelFeature)
                }
            }
            "spawn" {
                executes.asPlayer().args(
                    "destination" to Args.string().oneOf { get<OkiboTravelConfig>().allStations.map { it.id } },
                    "station" to Args.string().oneOf { get<OkiboTravelConfig>().allStations.map { it.id } }
                        .default { get<OkiboTravelConfig>().okiboStations.first().id },
                ) { destination, station ->
                    val config = get<OkiboTravelConfig>()
                    val destination = if (station == destination) {
                        config.okiboStations.firstOrNull { it.id != station }?.id ?: station
                    } else {
                        destination
                    }
                    spawnOkiboCart(
                        player,
                        config.allStations.find { it.id == station } ?: fail("Invalid station!"),
                        config.allStations.find { it.id == destination } ?: fail("Invalid destination!")
                    )
                }
            }
        }
    }
}