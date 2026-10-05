package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.coasters.TCCoasters
import com.bergerkiller.bukkit.coasters.tracks.TrackNodeSearchPath
import com.bergerkiller.bukkit.coasters.world.CoasterWorld
import com.bergerkiller.bukkit.common.BlockLocation
import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.pathfinding.PathWorld
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.editPlayerData
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.playerDataOrNull
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

private const val TRAIN_NAME = "OkiboCartPaid"

sealed interface TravelResult {
    data object Success : TravelResult
    data object SameStation : TravelResult
    data object NoRoute : TravelResult
    data object NotEnoughCoins : TravelResult
    data object SpawnFailed : TravelResult
}

class OkiboRepository(
    val config: OkiboTravelConfig,
    val logger: ComponentLogger,
) {
    private val tccoasters by lazy { Bukkit.getPluginManager().getPlugin("TCCoasters") as TCCoasters }
    private val pathProvider get() = TrainCarts.plugin.pathProvider

    /** null while TrainCarts has no route, free rides skip the route lookup */
    fun costBetween(origin: OkiboLineStation, destination: OkiboLineStation): Int? =
        if (config.costPerKM == 0.0) 0 else routeDistance(origin, destination)?.let(::cost)

    fun travel(player: Player, origin: OkiboLineStation, destination: OkiboLineStation): TravelResult {
        if (origin == destination) return TravelResult.SameStation
        val cost = routeDistance(origin, destination)?.let(::cost) ?: return TravelResult.NoRoute
        if (cost > (player.playerDataOrNull?.orthCoinsHeld ?: 0)) return TravelResult.NotEnoughCoins
        if (!spawnCart(player, origin, destination)) return TravelResult.SpawnFailed
        if (cost > 0) player.editPlayerData { orthCoinsHeld -= cost }
        return TravelResult.Success
    }

    fun spawnCart(player: Player, from: OkiboLineStation, to: OkiboLineStation): Boolean {
        val direction = direction(from, to) ?: run {
            logger.e("No okibo track leads from ${from.id} to ${to.id}")
            return false
        }
        val spawnGroup = SpawnableGroup.parse(TrainCarts.plugin, TRAIN_NAME)
        if (spawnGroup.members.isEmpty()) {
            logger.e("TrainCarts has no saved train named $TRAIN_NAME")
            return false
        }

        val spawnLocations = spawnGroup.findSpawnLocations(from.location, direction, SpawnableGroup.SpawnMode.DEFAULT)
        if (spawnLocations.locations.size < spawnGroup.members.size) {
            logger.e("Not enough track at ${from.id} to spawn a train")
            return false
        }
        spawnLocations.loadChunks()
        if (spawnLocations.occupiedLocations.isNotEmpty()) {
            logger.w("A train is already parked at ${from.id}")
            return false
        }

        val train = spawnGroup.spawn(spawnLocations)
        if (train == null || train.isEmpty()) {
            logger.e("TrainCarts failed to spawn a train at ${from.id}")
            return false
        }

        train.properties.apply {
            destination = to.id
            addTags("paid") // The ticket sign only launches trains tagged as paid
            setOwner(player.name, true)
            speedLimit = 1.0
            isSlowingDown = false
            collision = CollisionOptions.CANCEL
            isPlayerTakeable = false
            canOnlyOwnersEnter = true
            isManualMovementAllowed = true
        }
        train.head().addPassengerForced(player)

        logger.i("A train has been spawned at ${from.id} and is heading to ${to.id}!")
        return true
    }

    fun cost(railDistance: Double) = (railDistance * config.costPerKM / 1000).roundToInt()

    /** Rail distance in blocks, null while TrainCarts has no route between the two stations */
    fun railDistance(from: OkiboLineStation, to: OkiboLineStation): Double? {
        val start = pathNode(from) ?: return null
        val destination = pathNode(to) ?: return null
        return start.findConnection(destination)?.distance
    }

    /** null while TrainCarts has no route, a reroute is requested in that case */
    private fun routeDistance(origin: OkiboLineStation, destination: OkiboLineStation): Double? =
        railDistance(origin, destination) ?: run {
            // Without a destination sign no reroute can find the station
            if (pathNode(destination) != null) requestReroute(origin)
            null
        }

    /** Asks TrainCarts to rediscover routes from [station] after a player found one missing */
    fun requestReroute(station: OkiboLineStation) {
        if (pathProvider.isProcessing) return
        logger.w("Rediscovering okibo routes from ${station.id}, TrainCarts had none")
        when (val node = pathNode(station)) {
            null -> pathProvider.discoverFromRail(BlockLocation(station.location.block))
            else -> pathProvider.discoverFromNode(node)
        }
    }

    /**
     * TrainCarts only discovers path nodes from rails in loaded chunks, so after a cold start the okibo stations
     * have no routing info until someone stands there and the tracks are rerouted.
     */
    suspend fun warmUpRoutes() {
        val stations = config.okiboStations
        if (stations.isEmpty()) return

        holdingStationChunks(stations) {
            val missingNodes = stations.filter { pathNode(it) == null }
            if (missingNodes.isNotEmpty()) {
                logger.w("TrainCarts has no path nodes for ${missingNodes.map(OkiboLineStation::id)}, rediscovering them")
                missingNodes.forEach { pathProvider.discoverFromRail(BlockLocation(it.location.block)) }
                awaitRouting()
            }

            val unreachable = stations.filter { from -> stations.any { it != from && railDistance(from, it) == null } }
            if (unreachable.isNotEmpty()) {
                unreachable.forEach { station -> pathNode(station)?.let(pathProvider::discoverFromNode) }
                awaitRouting()
            }

            val unsigned = stations.filter { pathNode(it) == null }
            if (unsigned.isNotEmpty()) logger.e("No TrainCarts destination sign at okibo stations ${unsigned.map(OkiboLineStation::id)}")
            val signed = stations - unsigned.toSet()
            val broken = signed.filter { from -> signed.any { it != from && railDistance(from, it) == null } }
            when {
                broken.isEmpty() -> logger.s("Okibo line routes are ready")
                else -> logger.e("TrainCarts cannot route between all okibo stations, check ${broken.map(OkiboLineStation::id)}")
            }
        }
    }

    /**
     * PathProvider.addNewlyDiscovered drops a queued rail whose block reads as air, so a station in an unloaded
     * chunk is skipped without any error. Tickets keep the rails readable for the whole discovery pass.
     */
    private suspend fun <T> holdingStationChunks(stations: List<OkiboLineStation>, block: suspend () -> T): T {
        val plugin = abyss
        val chunks = stations.mapNotNull { station ->
            station.location.takeIf { it.isWorldLoaded }?.chunk?.also { it.addPluginChunkTicket(plugin) }
        }
        return try {
            block()
        } finally {
            chunks.forEach { it.removePluginChunkTicket(plugin) }
        }
    }

    private suspend fun awaitRouting() {
        withTimeoutOrNull(60.seconds) {
            while (pathProvider.isProcessing) delay(20.ticks)
        }
    }

    /** Direction a train has to face at [from] to start driving towards [to] */
    private fun direction(from: OkiboLineStation, to: OkiboLineStation): Vector? {
        val coasterWorld = tccoasters.getCoasterWorld(from.location.world) ?: return null
        val startNode = signedNodeAt(coasterWorld, from) ?: return null
        val destNode = signedNodeAt(coasterWorld, to) ?: return null
        val path = TrackNodeSearchPath.findShortest(startNode, mutableSetOf(destNode)) ?: return null
        return path.pathConnections.firstOrNull()?.getDirection(startNode)
    }

    private fun signedNodeAt(world: CoasterWorld, station: OkiboLineStation) =
        world.rails.findAtBlock(station.location.block).values().find { it.node().signs.isNotEmpty() }?.node()

    private fun pathNode(station: OkiboLineStation) =
        pathWorld(station)?.let { it.getNodeByName(station.id) ?: it.getNodeAtRail(station.location.block) }

    private fun pathWorld(station: OkiboLineStation): PathWorld? =
        station.location.takeIf { it.isWorldLoaded }?.let { pathProvider.getWorld(it.world) }
}
