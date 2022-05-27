package com.mineinabyss.misc
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("miscFeature")
class MiscFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(MiscListener())
    }
}