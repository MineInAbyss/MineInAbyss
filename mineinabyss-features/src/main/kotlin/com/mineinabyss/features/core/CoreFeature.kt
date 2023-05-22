package com.mineinabyss.features.core

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("core")
class CoreFeature : AbyssFeature {

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(CoreListener())
    }
}
