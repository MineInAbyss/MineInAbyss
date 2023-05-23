package com.mineinabyss.features.okibotravel.menu

import androidx.compose.runtime.Composable
import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboTraveler
import com.mineinabyss.features.okibotravel.OkiboTravelFeature
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.at
import com.mineinabyss.guiy.modifiers.size
import com.mineinabyss.idofront.messaging.broadcastVal
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

internal fun spawnOkiboCart(player: Player, station: OkiboLineStation, destination: OkiboLineStation) {
    val spawnGroup = SpawnableGroup.parse(TrainCarts.plugin, "OkiboCartPaid")
    val spawnLocations = spawnGroup.findSpawnLocations(station.location, station.location.direction, SpawnableGroup.SpawnMode.DEFAULT)
    val train = spawnGroup.spawn(spawnLocations)

    train.head().addPassengerForced(player)

    train.properties.destination = destination.name
    train.properties.addTags("paid") // Ticket adds this so that train only launches when player mounts it
    train.properties.setOwner(player.name, true)
    train.properties.speedLimit = 1.0
    train.properties.isSlowingDown = false
    train.properties.collision = CollisionOptions.CANCEL
    train.properties.isPlayerTakeable = false

    //checkStationDistance(train, destination).logVal("Distance to station")
    train.head().discoverRail().enterDirection().broadcastVal()
}

/*private fun checkStationDistance(train: MinecartGroup, destination: OkiboLineStation): Double {
    val start = train.head().railTracker.state
    val goal = RailState.getSpawnState(RailPiece.create(destination.location.block))

    // Measure distance between pos1 and pos2
    val measure = TrackWalkingPoint(start)
    var remaining: Double
    var bestRemaining = Double.MAX_VALUE
    var bestTotal = 0.0
    var foundGoalRailBlock = false
    while (measure.state.position().distance(goal.position()).also { remaining = it } > 1e-4) {
        if (measure.state.railPiece() == goal.railPiece()) {
            if (remaining < bestRemaining) {
                bestRemaining = remaining
                bestTotal = measure.movedTotal
            }
            foundGoalRailBlock = true
        } else if (foundGoalRailBlock) {
            // Somehow skipped it despite actually reaching this rail block
            // Just log the closest we've gotten to it
            measure.movedTotal = bestTotal
            break
        }
    }
    return measure.movedTotal
}*/

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
