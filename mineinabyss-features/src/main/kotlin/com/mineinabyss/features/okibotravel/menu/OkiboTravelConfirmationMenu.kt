package com.mineinabyss.features.okibotravel.menu

import androidx.compose.runtime.Composable
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import org.bukkit.entity.Player

@Composable
fun OkiboUIScope.OkiboTravelConfirmationMenu(
    player: Player,
    feature: OkiboTravelFeature,
    startingPoint: OkiboLineStation,
    destination: OkiboLineStation
) {
    //TODO Implement
    ConfirmTravel(player, Modifier.size(1, 1).at(0), feature, startingPoint, destination)
    CancelTravel(player, Modifier.size(1, 1).at(1), feature, startingPoint, destination)
}


@Composable
fun ConfirmTravel(
    player: Player,
    modifier: Modifier,
    feature: OkiboTravelFeature,
    station: OkiboLineStation,
    destination: OkiboLineStation
) {
    //TODO
    // Make button for station
    // Cost = okiboTraveler.mainStation
    val cost = OkiboTraveler(station.name).costTo(destination, feature.travelPoints) ?: return
}

fun spawnOkiboCart() {
    TODO("Not yet implemented")
}

@Composable
fun CancelTravel(
    player: Player,
    modifier: Modifier,
    feature: OkiboTravelFeature,
    station: OkiboLineStation,
    destination: OkiboLineStation
) {
    //TODO
    // Make button for station
    // Cost = okiboTraveler.mainStation
    val cost = OkiboTraveler(station.name).costTo(destination, feature.travelPoints) ?: return
}
