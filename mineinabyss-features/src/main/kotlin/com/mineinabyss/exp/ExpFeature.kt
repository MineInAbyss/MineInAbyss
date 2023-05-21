package com.mineinabyss.exp

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("exp")
class ExpFeature : AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        listeners(ExpListener())
    }
}
