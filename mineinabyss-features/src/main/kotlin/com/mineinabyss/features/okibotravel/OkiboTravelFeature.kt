package com.mineinabyss.features.okibotravel

import com.mineinabyss.dependencies.*
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.*

val OkiboTravelFeature = module("okibo-travel") {
    requirePlugins("BKCommonLib", "Train_Carts", "TCCoasters")
    require(get<AbyssFeatureConfig>().okiboTravel.enabled) { "Okibo travel feature is disabled" }

    val config by singleConfig<OkiboTravelConfig>("okiboTravel.yml")
    val repo by single { new(::OkiboRepository) }

    repo.removeOkiboMaps() // remove any old maps
    repo.spawnOkiboMaps()

    listeners(new(::OkiboTravelListener))

    addCloseable {
        repo.removeOkiboMaps()
    }
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
                get<OkiboRepository>().spawnCart(
                    player,
                    config.allStations.find { it.id == station } ?: fail("Invalid station!"),
                    config.allStations.find { it.id == destination } ?: fail("Invalid destination!")
                )
            }
        }
    }
}
