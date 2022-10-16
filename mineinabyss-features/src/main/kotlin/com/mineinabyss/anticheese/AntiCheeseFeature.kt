package com.mineinabyss.anticheese

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssContext
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("anticheese")
class AntiCheeseFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        if (AbyssContext.isGSitLoaded) listeners(GSitListener())
        listeners(AntiCheeseListener())
    }
}
