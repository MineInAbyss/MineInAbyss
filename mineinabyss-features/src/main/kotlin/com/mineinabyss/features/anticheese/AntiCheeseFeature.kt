package com.mineinabyss.features.anticheese

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("anticheese")
class AntiCheeseFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        if (abyss.isGSitLoaded) listeners(GSitListener())
        listeners(AntiCheeseListener())
    }
}
