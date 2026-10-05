package com.mineinabyss.features.overlay

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.nms.aliases.toNMS
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.network.protocol.game.*
import org.bukkit.entity.Player

// Clicks hit the player itself through the camera entity, which vanilla kicks for
class OverlayPacketHandler(private val player: Player) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        // A handler left behind by a reload must not schedule on a disabled plugin
        if (!abyss.isEnabled) return super.channelRead(ctx, msg)
        when (msg) {
            is ServerboundSetCarriedItemPacket -> return
            is ServerboundInteractPacket, is ServerboundAttackPacket, is ServerboundPunchPacket -> return sync { Overlays.click(player) }
            is ServerboundMovePlayerPacket -> if (msg.hasRotation()) {
                val (yaw, pitch) = msg.getYRot(0f) to msg.getXRot(0f)
                sync { Overlays.look(player, yaw, pitch) }
            }
        }
        super.channelRead(ctx, msg)
    }

    private fun sync(block: () -> Unit) {
        abyss.launch { block() }
    }

    companion object {
        private const val NAME = "mia_overlay"

        private fun pipeline(player: Player) = player.toNMS().connection.connection.channel.pipeline()

        fun install(player: Player) {
            uninstall(player)
            pipeline(player).addBefore("packet_handler", NAME, OverlayPacketHandler(player))
        }

        fun uninstall(player: Player) {
            pipeline(player).run { if (get(NAME) != null) remove(NAME) }
        }
    }
}
