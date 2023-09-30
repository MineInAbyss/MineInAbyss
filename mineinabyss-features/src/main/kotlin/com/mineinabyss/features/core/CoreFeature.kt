package com.mineinabyss.features.core

import com.comphenix.protocol.ProtocolLibrary
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("core")
class CoreFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        if (Plugins.isEnabled("ProtocolLib"))
            ProtocolLibrary.getProtocolManager().addPacketListener(ChestGuiPacket())

        listeners(CoreListener(), PreventSignEditListener())
    }
}
