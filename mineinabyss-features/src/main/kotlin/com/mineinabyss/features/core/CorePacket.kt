package com.mineinabyss.features.core

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.mineinabyss.idofront.font.Space
import com.mineinabyss.idofront.textcomponents.serialize
import com.mineinabyss.mineinabyss.core.abyss
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.BlockInventoryHolder

class CorePacket : PacketAdapter(
    abyss.plugin, ListenerPriority.LOWEST, PacketType.Play.Server.OPEN_WINDOW
) {
    //Override the normal
    override fun onPacketSending(event: PacketEvent) {
        val inventory = event.player.openInventory.topInventory
        if (inventory.holder !is BlockInventoryHolder/* && inventory.holder == event.player*/) return

        event.packet.chatComponents.write(
            0,
            WrappedChatComponent.fromText(
                when (inventory.type) {
                    InventoryType.CHEST -> "${Space.of(-8)}:vanilla_chest_${inventory.size / 9}:${Space.of(-167)}${event.player.openInventory.title().serialize()}"
                    //InventoryType.ANVIL -> "${Space.of(-61)}:vanilla_anvil:${Space.of(-105)}${event.player.openInventory.title}"
                    else -> return
                }
            )
        )
    }
}
