package com.mineinabyss.features.music

import com.mineinabyss.idofront.features.FeatureDSL
import com.mineinabyss.idofront.features.FeatureWithContext
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

class MusicFeature : FeatureWithContext<MusicContext>(::MusicContext) {
    override val dependsOn = setOf("WorldGuard")

    override fun FeatureDSL.enable() {
        plugin.listeners(context.queueMusicListener)
    }

    override fun FeatureDSL.disable() {
        HandlerList.unregisterAll(context.queueMusicListener)
        Bukkit.getServer().onlinePlayers.forEach {
            MusicScheduler.stopSchedulingMusic(it)
        }
    }
}
