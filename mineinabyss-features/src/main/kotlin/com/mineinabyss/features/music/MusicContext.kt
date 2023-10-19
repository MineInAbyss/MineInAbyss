package com.mineinabyss.features.music

import com.mineinabyss.idofront.config.config
import com.mineinabyss.mineinabyss.core.Configurable
import com.mineinabyss.mineinabyss.core.abyss

class MusicContext : Configurable<MusicConfig> {
    override val configManager = config("music", abyss.dataPath, MusicConfig())
    val queueMusicListener = QueueMusicListener()
}
