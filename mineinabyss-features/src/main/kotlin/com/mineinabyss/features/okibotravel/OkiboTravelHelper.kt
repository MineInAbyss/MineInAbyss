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

val mapEntities = mutableMapOf<OkiboMap, Int>()
val hitboxEntities = mutableMapOf<Pair<OkiboMap, OkiboMap.OkiboMapHitbox>, Int>()
val hitboxIconEntities = mutableMapOf<Pair<OkiboMap, OkiboMap.OkiboMapHitbox>, Int>()
val noticeBoardFurnitures = mutableSetOf<UUID>()

val OkiboMap.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == station }
val OkiboMap.OkiboMapHitbox.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == destStation }

fun getHitboxStation(entityId: Int) =
    hitboxEntities.entries.firstOrNull { it.value == entityId }?.key?.second
fun getMapEntityFromCollisionHitbox(id: Int) =
    hitboxEntities.entries.firstOrNull { it.value == id }?.key?.first

fun Player.sendOkiboMap(okiboMap: OkiboMap) {
    val connection = (this as CraftPlayer).handle.connection

    connection.send(ClientboundRemoveEntitiesPacket(
        *intArrayOf().toMutableList().apply {
            mapEntities.entries.firstOrNull { it.key.station == okiboMap.station }?.value?.let(::add)
            hitboxEntities.entries.firstOrNull { it.key.first.station == okiboMap.station }?.value?.let(::add)
            hitboxIconEntities.entries.firstOrNull { it.key.first.station == okiboMap.station }?.value?.let(::add)
        }.toIntArray()
    ))

    val textLoc = okiboMap.getStation?.location?.clone()?.add(okiboMap.offset)?.apply { yaw = okiboMap.yaw } ?: return
    val entityId = mapEntities.computeIfAbsent(okiboMap) { Entity.nextEntityId() }

    val textEntityPacket = ClientboundAddEntityPacket(
        entityId, UUID.randomUUID(), textLoc.x, textLoc.y, textLoc.z, textLoc.pitch, textLoc.yaw,
        EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
    )

    val txt = PaperAdventure.asVanilla(okiboMap.text.miniMsg()) ?: Component.empty()
    val textMetaPacket = ClientboundSetEntityDataPacket(
        entityId, listOf(
            SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, okiboMap.scale),
            SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, txt),
            SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
        )
    )

    connection.send(ClientboundBundlePacket(listOf(textEntityPacket, textMetaPacket)))

    okiboMap.hitboxes.flatMap { mapHitbox ->
        val packets = mutableListOf<Packet<ClientGamePacketListener>>()
        val hitboxEntityId = hitboxEntities.computeIfAbsent(okiboMap to mapHitbox) { Entity.nextEntityId() }
        val iconEntityId = hitboxIconEntities.computeIfAbsent(okiboMap to mapHitbox) { Entity.nextEntityId() }
        val loc = textLoc.clone().add(mapHitbox.offset)

        //interactionPacket
        ClientboundAddEntityPacket(
            hitboxEntityId, UUID.randomUUID(),
            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
            EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
        ).also(packets::add)

        //metadataPacket
        ClientboundSetEntityDataPacket(
            hitboxEntityId, listOf(
                SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, mapHitbox.hitbox.width.toFloat()),
                SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, mapHitbox.hitbox.height.toFloat()),
            )
        ).also(packets::add)

        okiboMap.icon?.also { icon ->
            val iconLoc = loc.clone().add(icon.offset)

            //iconPacket
            ClientboundAddEntityPacket(
                iconEntityId, UUID.randomUUID(),
                iconLoc.x, iconLoc.y, iconLoc.z, iconLoc.pitch, iconLoc.yaw,
                EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
            ).also(packets::add)

            //iconMetaPacket
            ClientboundSetEntityDataPacket(
                iconEntityId, listOf(
                    SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(icon.text.miniMsg()) ?: Component.empty()),
                    SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
                )
            ).also(packets::add)
        }
        packets
    }.also {
        connection.send(ClientboundBundlePacket(it))
    }
}

fun Player.removeOkiboMap(okiboMap: OkiboMap) {
    (this as CraftPlayer).handle.connection.send(
        ClientboundRemoveEntitiesPacket(
            *mapEntities.filterKeys { it != okiboMap }.values
                .plus(hitboxEntities.filterKeys { it.first != okiboMap }.values)
                .plus(hitboxIconEntities.filterKeys { it.first != okiboMap }.values).toIntArray()
        )
    )
}
