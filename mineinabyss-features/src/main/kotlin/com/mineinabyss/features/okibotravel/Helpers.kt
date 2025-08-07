package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.coasters.TCCoasters
import com.bergerkiller.bukkit.coasters.tracks.TrackNodeSearchPath
import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features.okiboLine
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.roundToInt

internal fun spawnOkiboCart(player: Player, station: OkiboLineStation, destination: OkiboLineStation) {
    val direction = station.direction(destination)
    val spawnGroup = SpawnableGroup.parse(TrainCarts.plugin, "OkiboCartPaid")
    val spawnLocations = spawnGroup.findSpawnLocations(station.location, direction, SpawnableGroup.SpawnMode.DEFAULT)
    val train = spawnGroup.spawn(spawnLocations)

    train.head().addPassengerForced(player)

    train.properties.destination = destination.id
    train.properties.addTags("paid") // Ticket adds this so that train only launches when player mounts it
    train.properties.setOwner(player.name, true)
    train.properties.speedLimit = 1.0
    train.properties.isSlowingDown = false
    train.properties.collision = CollisionOptions.CANCEL
    train.properties.isPlayerTakeable = false
    train.properties.canOnlyOwnersEnter = true
    train.properties.isManualMovementAllowed = true

    abyss.logger.i("A train has been spawned at ${station.id} and is heading to ${destination.id}!")
}

//TODO When substations become a thing, if index is -1 check all substations of every station for the current one etc
fun OkiboLineStation.costTo(destination: OkiboLineStation): Int? {
    val trainWorld = TrainCarts.plugin.pathProvider.getWorld(location.world)
    val startNode = trainWorld.getNodeByName(id) ?: trainWorld.getNodeAtRail(location.block) ?: return null
    val destNode = trainWorld.getNodeByName(destination.id) ?: trainWorld.getNodeAtRail(destination.location.block) ?: return null
    return (startNode.findConnection(destNode)?.distance?.times(okiboLine.config.costPerKM)?.div(1000))?.roundToInt()
}

val tccoasters by lazy { Bukkit.getPluginManager().getPlugin("TCCoasters") as TCCoasters }
fun OkiboLineStation.direction(destination: OkiboLineStation) : Vector {
    val tccWorld = tccoasters.getCoasterWorld(location.world)
    val startNode = tccWorld.rails.findAtBlock(location.block).values().find { it.node().signs.isNotEmpty() }?.node()!!
    val destNode = tccWorld.rails.findAtBlock(destination.location.block).values().find { it.node().signs.isNotEmpty() }?.node()!!
    val nextNode = TrackNodeSearchPath.findShortest(startNode, mutableSetOf(destNode)).pathConnections.first()

    return nextNode.getDirection(startNode)
}
