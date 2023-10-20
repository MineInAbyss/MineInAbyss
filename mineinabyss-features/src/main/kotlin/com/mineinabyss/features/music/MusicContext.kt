package com.mineinabyss.features.music

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.Configurable

class MusicContext : Configurable<MusicConfig> {
    override val configManager = config("music", abyss.dataPath, MusicConfig())
    val queueMusicListener = QueueMusicListener()
}
