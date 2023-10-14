package com.mineinabyss.features.music

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class QueueMusicListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        MusicScheduler.scheduleMusicPlaying(player)
    }
}
