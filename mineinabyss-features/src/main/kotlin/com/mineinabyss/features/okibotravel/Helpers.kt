package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features.okiboLine
import org.bukkit.entity.Player
import kotlin.math.roundToInt

internal fun spawnOkiboCart(player: Player, station: OkiboLineStation, destination: OkiboLineStation) {
    val direction = station.nextStation(destination)?.location?.toVector()?.subtract(station.location.toVector())?.normalize() ?: station.location.direction
    val spawnGroup = SpawnableGroup.parse(TrainCarts.plugin, "OkiboCartPaid")
    val spawnLocations = spawnGroup.findSpawnLocations(station.location, direction, SpawnableGroup.SpawnMode.DEFAULT)
    val train = spawnGroup.spawn(spawnLocations)

    train.head().addPassengerForced(player)

    train.properties.destination = destination.name
    train.properties.addTags("paid") // Ticket adds this so that train only launches when player mounts it
    train.properties.setOwner(player.name, true)
    train.properties.speedLimit = 1.0
    train.properties.isSlowingDown = false
    train.properties.collision = CollisionOptions.CANCEL
    train.properties.isPlayerTakeable = false
    train.properties.isManualMovementAllowed = true

    abyss.logger.i("A train has been spawned at ${station.name} and is heading to ${destination.name}!")
}

//TODO When substations become a thing, if index is -1 check all substations of every station for the current one etc
fun OkiboLineStation.costTo(destination: OkiboLineStation): Int? {
    val trainWorld = TrainCarts.plugin.pathProvider.getWorld(location.world)
    val startNode = trainWorld.getNodeByName(name) ?: trainWorld.getNodeAtRail(location.block) ?: return null
    val destNode = trainWorld.getNodeByName(destination.name) ?: trainWorld.getNodeAtRail(destination.location.block) ?: return null
    return (startNode.findConnection(destNode)?.distance?.times(okiboLine.config.costPerKM)?.div(1000))?.roundToInt()
}

val OkiboLineStation.isSubStation get() = okiboLine.config.okiboStations.any { this in it.subStations }
val OkiboLineStation.parentStation get() = okiboLine.config.okiboStations.firstOrNull { this in it.subStations }
fun OkiboLineStation.nextStation(destination: OkiboLineStation) : OkiboLineStation? {
    TrainCarts.plugin.pathProvider.getWorld(location.world).let {
        val (startNode, endNode) = (it.getNodeByName(name) ?: return null) to (it.getNodeByName(destination.name) ?: return null)
        val nextStation = startNode.findRoute(endNode).first().destination.name
        return okiboLine.config.allStations.find { it.name == nextStation }
    }
}
