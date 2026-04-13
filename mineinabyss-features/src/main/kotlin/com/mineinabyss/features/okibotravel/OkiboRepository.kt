package com.mineinabyss.features.okibotravel

import com.bergerkiller.bukkit.coasters.TCCoasters
import com.bergerkiller.bukkit.coasters.tracks.TrackNodeSearchPath
import com.bergerkiller.bukkit.tc.TrainCarts
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions
import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Vector3f
import java.util.*
import kotlin.math.roundToInt

class OkiboRepository(
    val config: OkiboTravelConfig,
    val logger: ComponentLogger,
) {
    val mapEntities = mutableMapOf<String, Int>()
    val hitboxEntities = mutableMapOf<String, MutableMap<String, Int>>()
    val hitboxIconEntities = mutableMapOf<String, MutableMap<String, Int>>()

    private val tccoasters by lazy { Bukkit.getPluginManager().getPlugin("TCCoasters") as TCCoasters }

    fun spawnCart(player: Player, station: OkiboLineStation, destination: OkiboLineStation) {
        val direction = direction(station, destination)
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

        logger.i("A train has been spawned at ${station.id} and is heading to ${destination.id}!")
    }

    //TODO When substations become a thing, if index is -1 check all substations of every station for the current one etc
    fun cost(station: OkiboLineStation, destination: OkiboLineStation): Int? {
        val trainWorld = TrainCarts.plugin.pathProvider.getWorld(station.location.world)
        val startNode = trainWorld.getNodeByName(station.id) ?: trainWorld.getNodeAtRail(station.location.block) ?: return null
        val destNode = trainWorld.getNodeByName(destination.id) ?: trainWorld.getNodeAtRail(destination.location.block) ?: return null
        return (startNode.findConnection(destNode)?.distance?.times(config.costPerKM)?.div(1000))?.roundToInt()
    }

    fun direction(station: OkiboLineStation, destination: OkiboLineStation): Vector {
        val tccWorld = tccoasters.getCoasterWorld(station.location.world)
        val startNode = tccWorld.rails.findAtBlock(station.location.block).values().find { it.node().signs.isNotEmpty() }?.node()!!
        val destNode = tccWorld.rails.findAtBlock(destination.location.block).values().find { it.node().signs.isNotEmpty() }?.node()!!
        val nextNode = TrackNodeSearchPath.findShortest(startNode, mutableSetOf(destNode)).pathConnections.first()

        return nextNode.getDirection(startNode)
    }

    fun getHitboxStation(entityId: Int): OkiboMap? = hitboxEntities.values
        .flatMap { it.toList() }
        .firstOrNull { it.second == entityId }
        ?.first
        ?.let { config.okiboMaps.firstOrNull { o -> it.startsWith(o.station) } }

    fun stationFor(map: OkiboMap) = config.okiboStations.firstOrNull { it.id == map.station }

    fun sendMap(player: Player, okiboMap: OkiboMap) {
        val connection = (player as CraftPlayer).handle.connection

        // Remove existing entities
        connection.send(
            ClientboundRemoveEntitiesPacket(
                *mutableListOf(mapEntities[okiboMap.station] ?: -1)
                    .plus(hitboxEntities[okiboMap.station]?.values ?: listOf())
                    .plus(hitboxIconEntities[okiboMap.station]?.values ?: listOf())
                    .toIntArray()
            )
        )

        // Map text entity
        val textLoc = okiboMap.location
        val entityId = mapEntities.computeIfAbsent(okiboMap.station) { Entity.nextEntityId() }
        val textEntityPacket = ClientboundAddEntityPacket(
            entityId, UUID.randomUUID(), textLoc.x, textLoc.y, textLoc.z, textLoc.pitch, textLoc.yaw - 90f,
            EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
        )
        val textMetaPacket = ClientboundSetEntityDataPacket(
            entityId, listOf(
                SynchedEntityData.DataValue(11, EntityDataSerializers.VECTOR3, okiboMap.offset),
                SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, okiboMap.scale),
                SynchedEntityData.DataValue(16, EntityDataSerializers.INT, (15 shl 4) or (15 shl 20)),
                SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(okiboMap.text)),
                SynchedEntityData.DataValue(25, EntityDataSerializers.INT, OkiboMap.background),
            )
        )
        val textBundle = ClientboundBundlePacket(listOf(textEntityPacket, textMetaPacket))
        connection.send(textBundle)

        // Process hitboxes and icons
        val hitboxBundles = okiboMap.hitboxes.flatMap { mapHitbox ->
            val packets = mutableListOf<ClientboundBundlePacket>()

            // Calculate hitbox position (offset from textLoc, rotated by yaw)
            val hitboxOffset = mapHitbox.offset.clone().rotateAroundY(Math.toRadians(-textLoc.yaw.toDouble()))
            val hitboxLoc = textLoc.clone().add(hitboxOffset)

            val hitboxEntityId = hitboxEntities.computeIfAbsent(okiboMap.station) { mutableMapOf() }
                .computeIfAbsent(mapHitbox.destStation) { Entity.nextEntityId() }
            val hitboxPacket = ClientboundAddEntityPacket(
                hitboxEntityId, UUID.randomUUID(),
                hitboxLoc.x, hitboxLoc.y, hitboxLoc.z, 0f, 0f,
                EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
            )
            val hitboxMetaPacket = ClientboundSetEntityDataPacket(
                hitboxEntityId, listOf(
                    SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, mapHitbox.hitbox.width.toFloat()),
                    SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, mapHitbox.hitbox.height.toFloat()),
                )
            )
            packets += ClientboundBundlePacket(listOf(hitboxPacket, hitboxMetaPacket))

            // Icon logic with translation offset from hitbox
            okiboMap.icon?.also { icon ->
                val iconEntityId = hitboxIconEntities.computeIfAbsent(okiboMap.station) { mutableMapOf() }
                    .computeIfAbsent(mapHitbox.destStation) { Entity.nextEntityId() }

                // Spawn icon at hitboxLoc
                val iconPacket = ClientboundAddEntityPacket(
                    iconEntityId, UUID.randomUUID(), hitboxLoc.x, hitboxLoc.y, hitboxLoc.z, textLoc.pitch, textLoc.yaw - 90,
                    EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
                )

                // Calculate icon translation (offset from hitbox, rotated by yaw)
                val iconTranslation = icon.offset.rotateY(Math.toRadians(-textLoc.yaw.toDouble()).toFloat(), Vector3f()) // Create a copy

                // Set translation metadata (ID 11) with Vector3 serializer
                val iconMetaPacket = ClientboundSetEntityDataPacket(
                    iconEntityId, listOf(
                        SynchedEntityData.DataValue(11, EntityDataSerializers.VECTOR3, iconTranslation), // Translation offset from hitbox
                        SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, icon.scale), // Translation offset from hitbox
                        SynchedEntityData.DataValue(16, EntityDataSerializers.INT, (15 shl 4) or (15 shl 20)),
                        SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(icon.text.miniMsg()) ?: Component.empty()),
                        SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0, 0, 0, 0).asARGB()),
                    )
                )

                packets += ClientboundBundlePacket(listOf(iconPacket, iconMetaPacket))
            }
            packets
        }

        hitboxBundles.forEach(connection::send)
    }

    fun removeMap(player: Player, okiboMap: OkiboMap) {
        (player as CraftPlayer).handle.connection.send(
            ClientboundRemoveEntitiesPacket(
                *listOf(mapEntities[okiboMap.station] ?: -1)
                    .plus(hitboxEntities[okiboMap.station]?.values ?: listOf())
                    .plus(hitboxIconEntities[okiboMap.station]?.values ?: listOf())
                    .toIntArray()
            )
        )
    }

    fun spawnOkiboMaps() {
        config.okiboMaps.forEach {
            if (!it.location.isWorldLoaded || !it.location.isChunkLoaded) return@forEach
            it.location.chunk.playersSeeingChunk.forEach { player ->
                sendMap(player, it)
            }
        }
    }

    fun removeOkiboMaps() {
        config.okiboMaps.forEach {
            if (!it.location.isWorldLoaded || !it.location.isChunkLoaded) return@forEach
            it.location.chunk.playersSeeingChunk.forEach { player ->
                removeMap(player, it)
            }
        }
    }
}