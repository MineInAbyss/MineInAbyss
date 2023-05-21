package com.mineinabyss.core

import com.comphenix.protocol.ProtocolLibrary
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit

@Serializable
@SerialName("core")
class CoreFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(CoreListener())
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
            ProtocolLibrary.getProtocolManager().addPacketListener(CorePacket())
    }
}
