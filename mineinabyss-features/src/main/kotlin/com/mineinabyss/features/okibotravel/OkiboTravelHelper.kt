package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.features.helpers.di.Features.okiboLine
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Color
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

val mapEntities = mutableMapOf<String, Int>()
val hitboxEntities = mutableMapOf<String, MutableMap<String, Int>>()
val hitboxIconEntities = mutableMapOf<String, MutableMap<String, Int>>()
val noticeBoardFurnitures = mutableMapOf<UUID, String>()

val OkiboMap.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == station }
val OkiboMap.OkiboMapHitbox.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == destStation }

fun getHitboxStation(entityId: Int) =
    hitboxEntities.values.flatMap { it.toList() }.firstOrNull { it.second == entityId }?.first?.let { okiboLine.config.okiboMaps.firstOrNull { o -> it.startsWith(o.station) } }

fun Player.sendOkiboMap(okiboMap: OkiboMap) {
    val connection = (this as CraftPlayer).handle.connection

    connection.send(ClientboundRemoveEntitiesPacket(
        *mutableListOf<Int>().apply {
            mapEntities.entries.firstOrNull { it.key == okiboMap.station }?.value?.let(::add)
            hitboxEntities.entries.firstOrNull { it.key == okiboMap.station }?.value?.values?.let(::addAll)
            hitboxIconEntities.entries.firstOrNull { it.key == okiboMap.station }?.value?.values?.let(::addAll)
        }.toIntArray()
    ))

    val textLoc = okiboMap.getStation?.location?.clone()?.add(okiboMap.offset)?.apply { yaw = okiboMap.yaw } ?: return
    val entityId = mapEntities.computeIfAbsent(okiboMap.station) { Entity.nextEntityId() }

    val textEntityPacket = ClientboundAddEntityPacket(
        entityId, UUID.randomUUID(), textLoc.x, textLoc.y, textLoc.z, textLoc.pitch, textLoc.yaw,
        EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
    )

    val textMetaPacket = ClientboundSetEntityDataPacket(
        entityId, listOf(
            SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, okiboMap.scale),
            SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(okiboMap.text)),
            SynchedEntityData.DataValue(25, EntityDataSerializers.INT, okiboMap.background), // Transparent background
        )
    )

    okiboMap.hitboxes.flatMap { mapHitbox ->
        val packets = mutableListOf<ClientboundBundlePacket>()
        val hitboxEntityId = hitboxEntities.computeIfAbsent(okiboMap.station) { mutableMapOf() }
            .computeIfAbsent(mapHitbox.destStation) { Entity.nextEntityId() }
        val iconEntityId = hitboxIconEntities.computeIfAbsent(okiboMap.station) { mutableMapOf() }
            .computeIfAbsent(mapHitbox.destStation) { Entity.nextEntityId() }
        val loc = textLoc.clone().add(mapHitbox.offset)

        //interactionPacket
        val addEntity = ClientboundAddEntityPacket(
            hitboxEntityId, UUID.randomUUID(),
            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
            EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
        )

        //metadataPacket
        val metadata = ClientboundSetEntityDataPacket(
            hitboxEntityId, listOf(
                SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, mapHitbox.hitbox.width.toFloat()),
                SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, mapHitbox.hitbox.height.toFloat()),
            )
        )
        packets += ClientboundBundlePacket(listOf(addEntity, metadata))

        okiboMap.icon?.also { icon ->
            val iconLoc = loc.clone().add(icon.offset)
            val iconPackets = mutableListOf<Packet<ClientGamePacketListener>>()

            //iconPacket
            ClientboundAddEntityPacket(
                iconEntityId, UUID.randomUUID(),
                iconLoc.x, iconLoc.y, iconLoc.z, iconLoc.pitch, iconLoc.yaw,
                EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
            ).also(iconPackets::add)

            //iconMetaPacket
            ClientboundSetEntityDataPacket(
                iconEntityId, listOf(
                    SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(icon.text.miniMsg()) ?: Component.empty()),
                    SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
                )
            ).also(iconPackets::add)

            packets += ClientboundBundlePacket(iconPackets)
        }
        packets
    }.plus(ClientboundBundlePacket(listOf(textEntityPacket, textMetaPacket))).forEach(connection::send)
}

fun Player.removeOkiboMap(okiboMap: OkiboMap) {
    (this as CraftPlayer).handle.connection.send(
        ClientboundRemoveEntitiesPacket(
            *mapEntities.filterKeys { it != okiboMap.station }.values
                .plus(hitboxEntities[okiboMap.station]?.values ?: listOf())
                .plus(hitboxIconEntities[okiboMap.station]?.values ?: listOf())
                .toIntArray()
        )
    )
}
