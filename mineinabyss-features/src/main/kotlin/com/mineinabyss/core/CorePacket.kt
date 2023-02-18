package com.mineinabyss.core

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.mineinabyss.core.mineInAbyss
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.BlockInventoryHolder

class CorePacket : PacketAdapter(
    mineInAbyss, ListenerPriority.LOWEST, PacketType.Play.Server.OPEN_WINDOW
) {
    //Override the normal
    override fun onPacketSending(event: PacketEvent) {
        val type = event.player.openInventory.topInventory
        if (type.type != InventoryType.CHEST || type.holder !is BlockInventoryHolder) return

        event.packet.chatComponents.write(
            0,
            //TODO Count inventory rows and +1
            //Shift text back 9 pixels, add the custom inv texture,
            //shift it back by the width of the texture - 9,
            //add the original lang text back
            WrappedChatComponent.fromText("${Space.of(-8)}:vanilla_chest_${type.size / 9}:${Space.of(-167)}${event.player.openInventory.title}"))
    }
}
