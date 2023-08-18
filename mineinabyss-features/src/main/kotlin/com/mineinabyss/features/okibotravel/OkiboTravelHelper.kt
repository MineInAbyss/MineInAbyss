package com.mineinabyss.features.okibotravel

import com.comphenix.protocol.events.PacketContainer
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.protocolburrito.dsl.sendTo
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Color
import java.util.*

val mapEntities = mutableMapOf<OkiboMap, Int>()
val hitboxEntities = mutableMapOf<Pair<OkiboMap, OkiboMap.OkiboMapHitbox>, Int>()

val OkiboMap.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == station }

fun getMapEntityFromCollisionHitbox(id: Int) =
    hitboxEntities.entries.firstOrNull { it.value == id }?.key?.first

fun spawnOkiboMaps() {
    okiboLine.config.okiboMaps.forEach { mapText ->
        val station = okiboLine.config.okiboStations.firstOrNull { it.name == mapText.station } ?: return@forEach
        val entityId = mapEntities.computeIfAbsent(mapText) { Entity.nextEntityId() }

        val textLoc = station.location.clone().add(mapText.offset)
        val spawnMapPacket = ClientboundAddEntityPacket(
            entityId, UUID.randomUUID(), textLoc.x, textLoc.y, textLoc.z, textLoc.pitch, textLoc.yaw,
            EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
        )
        Bukkit.getOnlinePlayers().forEach { PacketContainer.fromPacket(spawnMapPacket).sendTo(it) }

        val txt = Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(mapText.text.miniMsg().font(Key.key(mapText.font))))
        val metaPacket = ClientboundSetEntityDataPacket(
            entityId, listOf(
                SynchedEntityData.DataValue(11, EntityDataSerializers.VECTOR3, mapText.scale),
                SynchedEntityData.DataValue(22, EntityDataSerializers.COMPONENT, txt ?: Component.empty()),
                SynchedEntityData.DataValue(24, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
            )
        )
        Bukkit.getOnlinePlayers().forEach { PacketContainer.fromPacket(metaPacket).sendTo(it) }

        mapText.hitboxes.forEach { mapHitbox ->
            val hitboxEntityId = hitboxEntities.computeIfAbsent(mapText to mapHitbox) { Entity.nextEntityId() }
            val loc = textLoc.clone().add(mapHitbox.offset)
            val interactionPacket = ClientboundAddEntityPacket(
                hitboxEntityId, UUID.randomUUID(),
                loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
                EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
            )
            Bukkit.getOnlinePlayers().forEach { PacketContainer.fromPacket(interactionPacket).sendTo(it) }

            val metadataPacket = ClientboundSetEntityDataPacket(
                hitboxEntityId, listOf(
                    SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, mapHitbox.hitbox.width.toFloat()),
                    SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, mapHitbox.hitbox.height.toFloat()),
                )
            )
            Bukkit.getOnlinePlayers().forEach { PacketContainer.fromPacket(metadataPacket).sendTo(it) }
        }
    }
}
