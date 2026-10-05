package com.mineinabyss.features.okibotravel

import com.mineinabyss.dependencies.*
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.features.okibotravel.menu.OkiboMapMenu
import com.mineinabyss.features.overlay.OverlayListener
import com.mineinabyss.features.overlay.Overlays
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.features.*

val OkiboTravelFeature = module("okibo-travel") {
    requirePlugins("BKCommonLib", "Train_Carts", "TCCoasters", "Nexo", "Emojy")
    require(get<AbyssFeatureConfig>().okiboTravel.enabled) { "Okibo travel feature is disabled" }

    val config by singleConfig<OkiboTravelConfig>("okiboTravel.yml")
    val repo by single { new(::OkiboRepository) }
    single { new(::OkiboMapMenu) }

    listeners(new(::OkiboTravelListener), OverlayListener)
    addCloseable { Overlays.closeAll() }

    launch {
        repo.warmUpRoutes()
    }
}.mainCommand {
    "okibo" {
        "spawn" {
            executes.asPlayer().args(
                "destination" to Args.string().oneOf { get<OkiboTravelConfig>().okiboStations.map { it.id } },
                "station" to Args.string().oneOf { get<OkiboTravelConfig>().okiboStations.map { it.id } }
                    .default { get<OkiboTravelConfig>().okiboStations.first().id },
            ) { destination, station ->
                val config = get<OkiboTravelConfig>()
                if (station == destination) fail("A train cannot travel to the station it departs from!")
                val spawned = get<OkiboRepository>().spawnCart(
                    player,
                    config.station(station) ?: fail("Invalid station!"),
                    config.station(destination) ?: fail("Invalid destination!")
                )
                if (!spawned) fail("Could not spawn a train, see the console for details!")
            }
        }
    }
}
