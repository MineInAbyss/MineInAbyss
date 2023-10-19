package com.mineinabyss.features.music

import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.mineinabyss.core.AbyssFeatureWithContext
import com.mineinabyss.mineinabyss.core.MineInAbyssPlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

class MusicFeature : AbyssFeatureWithContext<MusicContext>(MusicContext::class) {
    override fun createContext() = MusicContext()

    override fun MineInAbyssPlugin.enableFeature() {
        listeners(context.queueMusicListener)
    }

    override fun MineInAbyssPlugin.disableFeature() {
        HandlerList.unregisterAll(context.queueMusicListener)
        Bukkit.getServer().onlinePlayers.forEach {
            MusicScheduler.stopSchedulingMusic(it)
        }
    }
}
