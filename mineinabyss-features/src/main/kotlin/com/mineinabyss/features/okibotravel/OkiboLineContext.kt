package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.idofront.di.DI
import org.bukkit.entity.Player

val okiboLine by DI.observe<OkiboLineContext>()
interface OkiboLineContext {
    val config: OkiboTravelConfig
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
}

//TODO When substations become a thing, if index is -1 check all substations of every station for the current one etc
fun OkiboLineStation.costTo(destination: OkiboLineStation): Int {
    val (startNode, endNode) = TrainCarts.plugin.pathProvider.getWorld(this.location.world).let {
        it.getNodeByName(this.name) to it.getNodeByName(destination.name)
    }
    return startNode.findRoute(endNode).size
}

val OkiboLineStation.isSubStation get() = okiboLine.config.okiboStations.any { this in it.subStations }
val OkiboLineStation.parentStation get() = okiboLine.config.okiboStations.firstOrNull { this in it.subStations }
