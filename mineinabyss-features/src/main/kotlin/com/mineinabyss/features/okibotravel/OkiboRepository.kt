package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.coasters.TCCoasters
import com.bergerkiller.bukkit.coasters.tracks.TrackNodeSearchPath
import com.bergerkiller.bukkit.coasters.world.CoasterWorld
import com.bergerkiller.bukkit.common.BlockLocation
import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.pathfinding.PathWorld
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityTypeIds
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Vector3f
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

private val textDisplayType = BuiltInRegistries.ENTITY_TYPE.getValue(EntityTypeIds.TEXT_DISPLAY)!!
private val interactionType = BuiltInRegistries.ENTITY_TYPE.getValue(EntityTypeIds.INTERACTION)!!

// Metadata indices, see net.minecraft.world.entity.Display and net.minecraft.world.entity.Interaction
private const val DISPLAY_TRANSLATION = 11
private const val DISPLAY_SCALE = 12
private const val DISPLAY_BRIGHTNESS = 16
private const val TEXT_DISPLAY_TEXT = 23
private const val TEXT_DISPLAY_BACKGROUND = 25
private const val INTERACTION_WIDTH = 8
private const val INTERACTION_HEIGHT = 9

/** Packed sky and block light, both maxed out */
private const val FULL_BRIGHT = (15 shl 4) or (15 shl 20)
private const val TRANSPARENT = 0

private const val TRAIN_NAME = "OkiboCartPaid"

class OkiboRepository(
    val config: OkiboTravelConfig,
    val logger: ComponentLogger,
) {
    private val spawnedMaps = mutableMapOf<String, SpawnedMap>()

    private val tccoasters by lazy { Bukkit.getPluginManager().getPlugin("TCCoasters") as TCCoasters }
    private val pathProvider get() = TrainCarts.plugin.pathProvider

    /** Entity ids of one map board, shared by every player that gets sent it */
    private class SpawnedMap(val map: OkiboMap, val textId: Int, val dots: List<Dot>) {
        class Dot(val destination: OkiboLineStation, val hitboxId: Int, val iconId: Int?)

        val entityIds = (listOf(textId) + dots.flatMap { listOfNotNull(it.hitboxId, it.iconId) }).toIntArray()
    }

    data class Target(val map: OkiboMap, val origin: OkiboLineStation, val destination: OkiboLineStation)

    fun mapAt(chunk: Chunk) = config.okiboMaps.firstOrNull {
        it.location.world == chunk.world && Chunk.getChunkKey(it.location) == chunk.chunkKey
    }

    /** The board and station a clicked dot belongs to, null when [entityId] is not one of ours */
    fun target(entityId: Int): Target? = spawnedMaps.values.firstNotNullOfOrNull { spawned ->
        val destination = spawned.dots.firstOrNull { it.hitboxId == entityId }?.destination ?: return@firstNotNullOfOrNull null
        val origin = config.station(spawned.map.station) ?: return@firstNotNullOfOrNull null
        Target(spawned.map, origin, destination)
    }

    fun sendMap(player: Player, map: OkiboMap) {
        val connection = (player as CraftPlayer).handle.connection
        val spawned = spawnedMaps.getOrPut(map.station) { allocate(map, player.handle.level()) }
        val board = map.location
        val boardRotation = Math.toRadians(-board.yaw.toDouble()).toFloat()

        connection.send(ClientboundRemoveEntitiesPacket(*spawned.entityIds))

        val packets = mutableListOf<Packet<in ClientGamePacketListener>>()
        packets += ClientboundAddEntityPacket(
            spawned.textId, UUID.randomUUID(), board.x, board.y, board.z, board.pitch, board.yaw - 90f,
            textDisplayType, 0, Vec3.ZERO, 0.0
        )
        packets += ClientboundSetEntityDataPacket(
            spawned.textId, listOf(
                DataValue(DISPLAY_TRANSLATION, EntityDataSerializers.VECTOR3, map.offset),
                DataValue(DISPLAY_SCALE, EntityDataSerializers.VECTOR3, map.scale),
                DataValue(DISPLAY_BRIGHTNESS, EntityDataSerializers.INT, FULL_BRIGHT),
                DataValue(TEXT_DISPLAY_TEXT, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(map.text)),
                DataValue(TEXT_DISPLAY_BACKGROUND, EntityDataSerializers.INT, TRANSPARENT),
            )
        )

        spawned.dots.forEach { dot ->
            val dotLoc = board.clone().add(dot.destination.iconHitboxOffset.rotatedBy(boardRotation))
            val size = config.hitboxSize.toFloat()

            // An interaction box grows upwards from its position, so drop it to center it on the dot
            packets += ClientboundAddEntityPacket(
                dot.hitboxId, UUID.randomUUID(), dotLoc.x, dotLoc.y - size / 2, dotLoc.z, 0f, 0f,
                interactionType, 0, Vec3.ZERO, 0.0
            )
            packets += ClientboundSetEntityDataPacket(
                dot.hitboxId, listOf(
                    DataValue(INTERACTION_WIDTH, EntityDataSerializers.FLOAT, size),
                    DataValue(INTERACTION_HEIGHT, EntityDataSerializers.FLOAT, size),
                )
            )

            val icon = map.icon ?: return@forEach
            val iconId = dot.iconId ?: return@forEach
            val iconLoc = board.clone()
                .add(Vector3f(dot.destination.iconHitboxOffset).add(icon.offset).rotatedBy(boardRotation))
            packets += ClientboundAddEntityPacket(
                iconId, UUID.randomUUID(), iconLoc.x, iconLoc.y, iconLoc.z, board.pitch, board.yaw - 90f,
                textDisplayType, 0, Vec3.ZERO, 0.0
            )
            packets += ClientboundSetEntityDataPacket(
                iconId, listOf(
                    DataValue(DISPLAY_SCALE, EntityDataSerializers.VECTOR3, icon.scale),
                    DataValue(DISPLAY_BRIGHTNESS, EntityDataSerializers.INT, FULL_BRIGHT),
                    DataValue(TEXT_DISPLAY_TEXT, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(icon.text)),
                    DataValue(TEXT_DISPLAY_BACKGROUND, EntityDataSerializers.INT, TRANSPARENT),
                )
            )
        }

        connection.send(ClientboundBundlePacket(packets))
    }

    fun removeMap(player: Player, map: OkiboMap) {
        val spawned = spawnedMaps[map.station] ?: return
        (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(*spawned.entityIds))
    }

    fun spawnOkiboMaps() {
        config.okiboMaps.forEach { map ->
            val loc = map.location
            if (!loc.isWorldLoaded || !loc.isChunkLoaded) return@forEach
            loc.chunk.playersSeeingChunk.forEach { sendMap(it, map) }
        }
    }

    /** Removes every map we sent out, also from players whose boards are no longer in a loaded chunk */
    fun removeOkiboMaps() {
        if (spawnedMaps.isEmpty()) return
        val packet = ClientboundRemoveEntitiesPacket(*spawnedMaps.values.flatMap { it.entityIds.asList() }.toIntArray())
        Bukkit.getOnlinePlayers().forEach { (it as CraftPlayer).handle.connection.send(packet) }
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

        val broken = stations.filter { from -> stations.any { it != from && railDistance(from, it) == null } }
        when {
            broken.isEmpty() -> logger.s("Okibo line routes are ready")
            else -> logger.e("TrainCarts cannot route between all okibo stations, check ${broken.map(OkiboLineStation::id)}")
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

    private fun allocate(map: OkiboMap, level: ServerLevel) = SpawnedMap(
        map = map,
        textId = level.nextEntityId,
        dots = config.destinationsOn(map).map {
            SpawnedMap.Dot(it, level.nextEntityId, if (map.icon != null) level.nextEntityId else null)
        }
    )
}

private fun Vector3f.rotatedBy(yawRadians: Float) = Vector3f(this).rotateY(yawRadians).let { Vector(it.x, it.y, it.z) }
