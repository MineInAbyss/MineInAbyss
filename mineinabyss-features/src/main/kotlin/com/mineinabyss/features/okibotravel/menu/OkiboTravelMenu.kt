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
fun OkiboUIScope.OkiboTravelMenu(player: Player, feature: OkiboTravelFeature, okiboTraveler: OkiboTraveler) {
    feature.travelPoints.forEachIndexed { index, station ->
        //TODO modifier.at to place in grid based on index
        TravelPointButton(player, Modifier.size(1,1).at(index), feature, station, okiboTraveler)
    }
}

@Composable
fun TravelPointButton(player: Player, modifier: Modifier, feature: OkiboTravelFeature, station: OkiboLineStation, okiboTraveler: OkiboTraveler) {
    //TODO
    // Make button for station
    // Cost = okiboTraveler.mainStation
    val cost = okiboTraveler.costTo(station, feature.travelPoints) ?: return
}
