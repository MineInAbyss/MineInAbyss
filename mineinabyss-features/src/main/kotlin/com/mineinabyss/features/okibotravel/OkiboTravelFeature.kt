package com.mineinabyss.features.okibotravel

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.*

val OkiboTravelFeature = module("okibo-travel") {
    requirePlugins("BKCommonLib", "Train_Carts", "TCCoasters")
    require(get<AbyssFeatureConfig>().okiboTravel.enabled) { "Okibo travel feature is disabled" }

    val config by singleConfig<OkiboTravelConfig>("okiboTravel.yml")
    removeOkiboMaps()

    //TODO no singletons!!
    mapEntities.clear()
    hitboxEntities.clear()
    hitboxIconEntities.clear()

    spawnOkiboMaps()

    listeners(new(::OkiboTravelListener))
}.mainCommand {
    "okibo" {
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
