package com.mineinabyss.features.music

import com.mineinabyss.dependencies.*
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.singleConfig
import org.bukkit.Bukkit

val MusicFeature = module("music") {
    require(get<AbyssFeatureConfig>().music.enabled) { "Music feature is disabled" }

    val config by singleConfig<MusicConfig>("music.yml")
    val scheduler by single { new(::MusicScheduler) }
    listeners(new(::QueueMusicListener))

    addCloseable {
        Bukkit.getServer().onlinePlayers.forEach(scheduler::stopSchedulingMusic)
    }
}
