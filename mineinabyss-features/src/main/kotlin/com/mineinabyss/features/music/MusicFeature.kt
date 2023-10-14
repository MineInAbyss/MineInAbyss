package com.mineinabyss.features.music

import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeature
import com.mineinabyss.mineinabyss.core.Configurable
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import com.mineinabyss.mineinabyss.core.abyss
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("music")
class MusicFeature : AbyssFeature, Configurable<MusicConfig> {
    @Transient
    override val configManager = config("music", abyss.dataPath, MusicConfig())

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(QueueMusicListener())
    }
}
