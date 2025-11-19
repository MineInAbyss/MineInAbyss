package com.mineinabyss.features.music

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class QueueMusicListener(
    val scheduler: MusicScheduler
) : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        scheduler.scheduleMusicPlaying(player)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        scheduler.stopSchedulingMusic(player)
    }
}
