package com.mineinabyss.features.music

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.music.NowPlaying
import com.mineinabyss.components.music.RecentlyPlayed
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.mineinabyss.core.abyss
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds

object MusicScheduler {
    private val conf get() = Features.music.config
    private val regionContainer = WorldGuard.getInstance().platform.regionContainer
    fun scheduleMusicPlaying(player: Player) {
        abyss.plugin.launch {
            while (player.isConnected) {
                logInfo("Starting music scheduler for ${player.name}")
                val playable = getPlayableSongsAtLocation(player.location)
                if (playable.isEmpty()) delay(conf.maxSongWaitTime)
                else {
                    val recentlyPlayed = player.toGeary().getOrSet { RecentlyPlayed(setOf()) }
                    val notRecentlyPlayed = playable.filter { it !in recentlyPlayed.songs }
                    logInfo("Recently played: $recentlyPlayed")
                    logInfo("Playable: $playable")
                    val chooseFrom = if(notRecentlyPlayed.isEmpty()) {
                        player.toGeary().set(RecentlyPlayed(setOf()))
                        notRecentlyPlayed
                    } else playable
                    val song = chooseFrom.random()
                    logInfo("Playing $song")
                    playSongIfNotPlaying(song, player)
                    logInfo("Finished playing $song")

                    // Choose a random wait time as defined in config
                    delay((conf.minSongWaitTime.inWholeSeconds..conf.maxSongWaitTime.inWholeSeconds).random().seconds)
                }
            }

        }
    }

    fun getPlayableSongsAtLocation(location: Location): List<String> {
        return regionContainer
            .createQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location))
            .mapNotNull { conf.region2songs[it.id] }
    }


    suspend fun playSongIfNotPlaying(songName: String, player: Player) {
        val song = conf.songs[songName] ?: return
        player.toGeary().apply {
            if (has<NowPlaying>()) return
            set(NowPlaying(song, System.currentTimeMillis()))
            val recents = getOrSet { RecentlyPlayed(setOf()) }
            set(RecentlyPlayed(recents.songs + songName))
        }
        player.playSound(player.location, song.key, SoundCategory.AMBIENT, 1f, 1f)
        delay(song.duration)
        if (player.isConnected) player.toGeary().remove<NowPlaying>()
    }
}
