package com.mineinabyss.invisibleItemFrames
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("invisibleItemFrames")
class InvisibleItemFramesFeature: AbyssFeature {
    override fun MineInAbyssPlugin.enableFeature() {
        registerEvents(InteractionListener())
    }
}