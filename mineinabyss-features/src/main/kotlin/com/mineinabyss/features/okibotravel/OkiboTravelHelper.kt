package com.mineinabyss.features.okibotravel

import com.mineinabyss.blocky.api.BlockyFurnitures
import com.mineinabyss.blocky.helpers.GenericHelpers.toEntity
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.features.helpers.di.Features.okiboLine
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
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import java.util.*

val mapEntities = mutableMapOf<OkiboMap, Int>()
val hitboxEntities = mutableMapOf<Pair<OkiboMap, OkiboMap.OkiboMapHitbox>, Int>()
val hitboxIconEntities = mutableMapOf<Pair<OkiboMap, OkiboMap.OkiboMapHitbox>, Int>()
val noticeBoardFurnitures = mutableMapOf<OkiboMap, Pair<UUID, Location>>()

val OkiboMap.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == station }
val OkiboMap.OkiboMapHitbox.getStation get() = okiboLine.config.okiboStations.firstOrNull { it.name == destStation }

fun getHitboxStation(entityId: Int) =
    hitboxEntities.entries.firstOrNull { it.value == entityId }?.key?.second
fun getMapEntityFromCollisionHitbox(id: Int) =
    hitboxEntities.entries.firstOrNull { it.value == id }?.key?.first

internal fun spawnOkiboMaps() = Bukkit.getOnlinePlayers().forEach { it.sendOkiboMaps() }
internal fun Player.sendOkiboMaps() = okiboLine.config.okiboMaps.forEach(::sendOkiboMap)
internal fun Player.clearOkiboMaps() {
    (this as CraftPlayer).handle.connection.let {
        it.send(ClientboundRemoveEntitiesPacket(*mapEntities.values.toIntArray()))
        it.send(ClientboundRemoveEntitiesPacket(*hitboxEntities.values.toIntArray()))
        it.send(ClientboundRemoveEntitiesPacket(*hitboxIconEntities.values.toIntArray()))
    }

    noticeBoardFurnitures.values.forEach {
        it.second.world?.getChunkAtAsync(location)?.thenRun {
            if (BlockyFurnitures.removeFurniture(it.second) == true) return@thenRun
            else (it.first.toEntity() as? ItemDisplay)?.let(BlockyFurnitures::removeFurniture)
        }
    }
}
private fun Player.sendOkiboMap(okiboMap: OkiboMap) {
    val connection = (this as CraftPlayer).handle.connection

    connection.send(ClientboundRemoveEntitiesPacket(
        *intArrayOf().toMutableList().apply {
            mapEntities.entries.firstOrNull { it.key.station == okiboMap.station }?.value?.let(::add)
            hitboxEntities.entries.firstOrNull { it.key.first.station == okiboMap.station }?.value?.let(::add)
            hitboxIconEntities.entries.firstOrNull { it.key.first.station == okiboMap.station }?.value?.let(::add)
        }.toIntArray()
    ))

    noticeBoardFurnitures.entries.firstOrNull { it.key.station == okiboMap.station }?.value?.let {
        it.second.world?.getChunkAtAsync(it.second)?.thenRun {
            if (BlockyFurnitures.removeFurniture(it.second) == true) return@thenRun
            else (it.first.toEntity() as? ItemDisplay)?.let(BlockyFurnitures::removeFurniture)
        }
    }

    val textLoc = okiboMap.getStation?.location?.clone()?.add(okiboMap.offset)?.apply { yaw = okiboMap.yaw } ?: return
    val entityId = mapEntities.computeIfAbsent(okiboMap) { Entity.nextEntityId() }

    val textEntityPacket = ClientboundAddEntityPacket(
        entityId, UUID.randomUUID(), textLoc.x, textLoc.y, textLoc.z, textLoc.pitch, textLoc.yaw,
        EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
    )

    val txt = PaperAdventure.asVanilla(okiboMap.text.miniMsg())  ?: Component.empty()
    val textMetaPacket = ClientboundSetEntityDataPacket(
        entityId, listOf(
            SynchedEntityData.DataValue(12, EntityDataSerializers.VECTOR3, okiboMap.scale),
            SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, txt),
            SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
        )
    )

    connection.send(ClientboundBundlePacket(listOf(textEntityPacket, textMetaPacket)))

    okiboMap.hitboxes.forEach { mapHitbox ->
        val hitboxEntityId = hitboxEntities.computeIfAbsent(okiboMap to mapHitbox) { Entity.nextEntityId() }
        val iconEntityId = hitboxIconEntities.computeIfAbsent(okiboMap to mapHitbox) { Entity.nextEntityId() }
        val loc = textLoc.clone().add(mapHitbox.offset)

        //interactionPacket
        val hitboxEntityPacket = ClientboundAddEntityPacket(
            hitboxEntityId, UUID.randomUUID(),
            loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
            EntityType.INTERACTION, 0, Vec3.ZERO, 0.0
        )

        //metadataPacket
        val hitboxMetaPacket = ClientboundSetEntityDataPacket(
            hitboxEntityId, listOf(
                SynchedEntityData.DataValue(8, EntityDataSerializers.FLOAT, mapHitbox.hitbox.width.toFloat()),
                SynchedEntityData.DataValue(9, EntityDataSerializers.FLOAT, mapHitbox.hitbox.height.toFloat()),
            )
        )

        connection.send(ClientboundBundlePacket(listOf(hitboxEntityPacket, hitboxMetaPacket)))

        okiboMap.icon?.let {
            val iconLoc = loc.clone().add(it.offset)

            //iconPacket
            val iconEntityPacket = ClientboundAddEntityPacket(
                iconEntityId, UUID.randomUUID(),
                iconLoc.x, iconLoc.y, iconLoc.z, iconLoc.pitch, iconLoc.yaw,
                EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
            )

            //iconMetaPacket
            val iconMetaPacket = ClientboundSetEntityDataPacket(
                iconEntityId, listOf(
                    SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, PaperAdventure.asVanilla(it.text.miniMsg()) ?: Component.empty()),
                    SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0,0,0,0).asARGB()), // Transparent background
                )
            )

            connection.send(ClientboundBundlePacket(listOf(iconEntityPacket, iconMetaPacket)))
        }
    }

    okiboMap.noticeBoardFurniture?.let { noticeBoard ->
        val boardLoc = textLoc.clone().add(noticeBoard.offset)
        val entity = BlockyFurnitures.placeFurniture(noticeBoard.prefabKey, boardLoc, noticeBoard.yaw) ?: return@let
        noticeBoardFurnitures[okiboMap]?.let { (it.first.toEntity() as? ItemDisplay)?.let(BlockyFurnitures::removeFurniture) }
        noticeBoardFurnitures[okiboMap] = entity.uniqueId to boardLoc
        entity.isPersistent = false
    }
}
