package com.mineinabyss.features.blocklocker

import com.mineinabyss.blocky.helpers.GenericHelpers.toBlockCenterLocation
import com.mineinabyss.components.blocklocker.BlockLockerDebug
import com.mineinabyss.components.blocklocker.BlockLockerLock
import com.mineinabyss.features.helpers.BlockLockerHelpers
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.systems.builders.system
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.entities.toOfflinePlayer
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.operators.minus
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.time.ticks
import io.papermc.paper.adventure.PaperAdventure
import it.unimi.dsi.fastutil.ints.IntList
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
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.TileState
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory
import java.util.*
import kotlin.math.roundToInt

fun GearyModule.createBlockLockerDebugTextSystem() = system(query<Player>()).every(4.ticks).exec { (player) ->
    if (player.isConnected && player.toGearyOrNull()?.has<BlockLockerDebug>() == true) {
        BlockLockerDebugText.sendBlockLockerDebug(player)
    } else BlockLockerDebugText.removeBlockLockerDebug(player)
}

typealias PlayerUUID = UUID
object BlockLockerDebugText {
    private val debugIdMap = mutableMapOf<PlayerUUID, Pair<Location, Int>>()
    private fun createDebugText(lock: BlockLockerLock) = PaperAdventure.asVanilla(
        """
            <yellow>Container-Owner: <gold>${lock.owner.toOfflinePlayer().name}
            <gray>Allowed Users:<yellow>
            ${lock.allowedPlayers.filter { it != lock.owner }.joinToString("\n") { it.toOfflinePlayer().name.toString() }}
        """.trimIndent().miniMsg()
    )

    fun sendBlockLockerDebug(player: Player) {
        val targetBlock = player.getTargetBlockExact(5, FluidCollisionMode.NEVER) ?: return removeBlockLockerDebug(player)
        val tileState = BlockLockerHelpers.blockLockerTilestate(targetBlock) ?: return removeBlockLockerDebug(player)
        val lock = BlockLockerHelpers.blockLockerLock(targetBlock) ?: return removeBlockLockerDebug(player)
        val loc = tileState.debugTextLoc()

        var textEntityPacket: ClientboundAddEntityPacket? = null
        val (tileLocation, entityId) = debugIdMap.getOrPut(player.uniqueId) {
            ClientboundAddEntityPacket(
                Entity.nextEntityId(), UUID.randomUUID(),
                loc.x, loc.y, loc.z, loc.pitch, loc.yaw,
                EntityType.TEXT_DISPLAY, 0, Vec3.ZERO, 0.0
            ).let {
                textEntityPacket = it
                tileState.location to it.id
            }
        }

        if (tileLocation != tileState.location) return removeBlockLockerDebug(player)

        val textMetaPacket = ClientboundSetEntityDataPacket(
            entityId, listOf(
                SynchedEntityData.DataValue(15, EntityDataSerializers.BYTE, 1), // Billboard
                SynchedEntityData.DataValue(23, EntityDataSerializers.COMPONENT, createDebugText(lock) ?: Component.empty()),
                SynchedEntityData.DataValue(25, EntityDataSerializers.INT, Color.fromARGB(0, 0, 0, 0).asARGB()), // Transparent background
                SynchedEntityData.DataValue(27, EntityDataSerializers.BYTE, ((0 or 0x01) or (0 and 0x0F shl 3)).toByte())
            )
        )

        (player as CraftPlayer).handle.connection.send(
            textEntityPacket?.let { ClientboundBundlePacket(listOf(textEntityPacket, textMetaPacket)) } ?: textMetaPacket
        )
    }

    fun removeBlockLockerDebug(player: Player) {
        debugIdMap.remove(player.uniqueId)?.let {
            (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(IntList.of(*intArrayOf(it.second))))
        }
    }

    private fun TileState.debugTextLoc(): Location {
        return (((this as? Chest)?.inventory as? DoubleChestInventory)?.location?.add(0.5, 1.0, 0.5) ?: location.toBlockCenterLocation().up(1))
    }


}