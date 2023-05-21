package com.mineinabyss.anticheese

import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import dev.geco.gsit.GSitMain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("anticheese")
class AntiCheeseFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        Plugins.isEnabled<GSitMain>()
        if (abyss.isGSitLoaded) listeners(GSitListener())
        listeners(AntiCheeseListener())
    }
}
