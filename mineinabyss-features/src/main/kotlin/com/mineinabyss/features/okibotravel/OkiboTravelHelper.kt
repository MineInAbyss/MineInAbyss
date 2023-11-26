package com.mineinabyss.features.okibotravel

import com.comphenix.protocol.events.PacketContainer
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.features.helpers.di.Features.okiboLine
import com.mineinabyss.idofront.messaging.broadcastVal
import com.mineinabyss.idofront.messaging.logVal
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.protocolburrito.dsl.sendTo
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Player
import java.util.*

val mapEntities = mutableMapOf<OkiboMap, Int>()
val hitboxEntities = mutableMapOf<Pair<OkiboMap, OkiboMap.OkiboMapHitbox>, Int>()

val OkiboMap.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == station }
val OkiboMap.OkiboMapHitbox.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == destStation }

fun getHitboxStation(entityId: Int) =
    hitboxEntities.entries.firstOrNull { it.value == entityId }?.key?.second
fun getMapEntityFromCollisionHitbox(id: Int) =
    hitboxEntities.entries.firstOrNull { it.value == id }?.key?.first

internal fun spawnOkiboMaps() = Bukkit.getOnlinePlayers().forEach { it.sendOkiboMaps() }
internal fun Player.sendOkiboMaps() = okiboLine.config.okiboMaps.forEach(::sendOkiboMap)
internal fun Player.clearOkiboMaps() {
    PacketContainer.fromPacket(ClientboundRemoveEntitiesPacket(*mapEntities.values.toIntArray())).sendTo(this)
    PacketContainer.fromPacket(ClientboundRemoveEntitiesPacket(*hitboxEntities.values.toIntArray())).sendTo(this)
}
private fun Player.sendOkiboMap(okiboMap: OkiboMap) {
    val textLoc = okiboMap.getStation?.location?.clone()?.add(okiboMap.offset) ?: return
    val entityId = mapEntities.computeIfAbsent(okiboMap) { Entity.nextEntityId() }
    val spawnMapPacket = ClientboundAddEntityPacket(
        entityId, UUID.randomUUID(), textLoc.x, textLoc.y, textLoc.z, textLoc.pitch, textLoc.yaw,
        EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
    )
    PacketContainer.fromPacket(spawnMapPacket).sendTo(this)

    val txt = Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(okiboMap.text.miniMsg()))
    val metaPacket = ClientboundSetEntityDataPacket(
        entityId, listOf(
            SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, okiboMap.scale),
            SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, txt ?: Component.empty()),
            SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
        )
    )

    PacketContainer.fromPacket(metaPacket).sendTo(this)

    okiboMap.hitboxes.forEach { mapHitbox ->
        val hitboxEntityId = hitboxEntities.computeIfAbsent(okiboMap to mapHitbox) { Entity.nextEntityId() }
        val loc = textLoc.clone().add(mapHitbox.offset)
        val interactionPacket = ClientboundAddEntityPacket(
            hitboxEntityId, UUID.randomUUID(),
            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
            EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
        )
        PacketContainer.fromPacket(interactionPacket).sendTo(this)

        val metadataPacket = ClientboundSetEntityDataPacket(
            hitboxEntityId, listOf(
                SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, mapHitbox.hitbox.width.toFloat()),
                SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, mapHitbox.hitbox.height.toFloat()),
            )
        )
        PacketContainer.fromPacket(metadataPacket).sendTo(this)
    }
}
