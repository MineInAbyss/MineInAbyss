package com.mineinabyss.features.music

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class QueueMusicListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        MusicScheduler.scheduleMusicPlaying(player)
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        MusicScheduler.stopSchedulingMusic(player)
    }
}
