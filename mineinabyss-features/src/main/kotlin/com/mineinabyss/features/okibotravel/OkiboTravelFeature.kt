package com.mineinabyss.features.okibotravel

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.playerExecutes
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.error
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
                playerExecutes(
                    Args.options { get<OkiboTravelConfig>().allStations.map { it.id } },
                    Args.options { get<OkiboTravelConfig>().allStations.map { it.id } }
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
                        config.allStations.find { it.id == station }
                            ?: return@playerExecutes player.error("Invalid station!"),
                        config.allStations.find { it.id == destination }
                            ?: return@playerExecutes player.error("Invalid destination!")
                    )
                }
            }
        }
    }
}