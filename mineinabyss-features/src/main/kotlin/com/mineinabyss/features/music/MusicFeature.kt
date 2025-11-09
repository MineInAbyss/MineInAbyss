package com.mineinabyss.features.music

import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

val MusicFeature = feature("music") {
    override fun FeatureDSL.enable() {
        plugin.listeners(context.queueMusicListener)
    }

    override fun FeatureDSL.disable() {
        HandlerList.unregisterAll(context.queueMusicListener)
        Bukkit.getServer().onlinePlayers.forEach(MusicScheduler::stopSchedulingMusic)
    }
}
