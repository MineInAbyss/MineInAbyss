package com.mineinabyss.features.music

import com.mineinabyss.features.abyss
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.feature
import org.bukkit.Bukkit
import org.koin.core.module.dsl.scopedOf

val MusicFeature = feature("music") {
    dependsOn { plugins("DeeperWorld") }
    scopedModule {
        scoped<MusicConfig> { config("music", abyss.dataPath, MusicConfig()).getOrLoad() }
        scopedOf(::MusicScheduler)
        scopedOf(::QueueMusicListener)
    }

    onEnable {
        listeners(get<QueueMusicListener>())
    }

    onDisable {
        val scheduler = get<MusicScheduler>()
        Bukkit.getServer().onlinePlayers.forEach(scheduler::stopSchedulingMusic)
    }
}
