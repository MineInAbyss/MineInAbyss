package com.mineinabyss.features.core

import com.mineinabyss.idofront.features.Feature
import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.plugin.listeners

class CoreFeature : Feature() {

    override fun FeatureDSL.enable() {
        /*if (Plugins.isEnabled("ProtocolLib"))
            ProtocolLibrary.getProtocolManager().addPacketListener(ChestGuiPacket())*/

        plugin.listeners(CoreListener(), PreventSignEditListener())
    }
}
