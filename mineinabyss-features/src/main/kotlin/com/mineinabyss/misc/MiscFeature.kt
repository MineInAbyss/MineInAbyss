package com.mineinabyss.misc
import com.comphenix.protocol.ProtocolLibrary
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit

@Serializable
@SerialName("misc")
class MiscFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(MiscListener())
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib"))
            ProtocolLibrary.getProtocolManager().addPacketListener(MiscPackets())
    }
}
