package com.mineinabyss.features.misc

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class MiscFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(MiscListener())
    }
}
