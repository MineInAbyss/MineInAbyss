package com.mineinabyss.features.music

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.music.NowPlaying
import com.mineinabyss.components.music.RecentlyPlayed
import com.mineinabyss.components.music.ScheduledMusicJob
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.messaging.logInfo
import com.mineinabyss.mineinabyss.core.abyss
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object MusicScheduler {
    private val conf get() = Features.music.config
    private val regionContainer = WorldGuard.getInstance().platform.regionContainer

    fun stopSchedulingMusic(player: Player) {
        player.toGeary().apply {
            get<ScheduledMusicJob>()?.job?.cancel()
            remove<NowPlaying>()
            remove<ScheduledMusicJob>()
        }
    }

    fun scheduleMusicPlaying(player: Player) {
        val musicPlayingJob = abyss.plugin.launch {
            val waitOnLogin = chooseTimeBetween(conf.minWaitTimeOnLogin, conf.maxWaitTimeOnLogin)
            logInfo("Starting music scheduler for ${player.name}, waiting $waitOnLogin before playing.")
            delay(waitOnLogin)
            while (player.isConnected) {
                val playable = getPlayableSongsAtLocation(player.location)
                if (playable.isEmpty()) delay(conf.maxSongWaitTime)
                else {
                    val recentlyPlayed = player.toGeary().getOrSet { RecentlyPlayed(setOf()) }
                    val notRecentlyPlayed = playable.filter { it !in recentlyPlayed.songs }
                    logInfo("Recently played: $recentlyPlayed")
                    logInfo("Playable: $playable")
                    val chooseFrom = if (notRecentlyPlayed.isEmpty()) {
                        player.toGeary().set(RecentlyPlayed(setOf()))
                        notRecentlyPlayed
                    } else playable
                    val song = chooseFrom.random()
                    logInfo("Playing $song")
                    playSongIfNotPlaying(song, player)

                    // Choose a random wait time as defined in config
                    val wait = chooseTimeBetween(conf.minSongWaitTime, conf.maxSongWaitTime)
                    logInfo("Finished playing $song, waiting $wait before playing another.")
                    delay(wait)
                }
            }
        }
        player.toGeary().set(ScheduledMusicJob(musicPlayingJob))
    }

    fun getPlayableSongsAtLocation(location: Location): List<String> {
        return regionContainer
            .createQuery()
            .getApplicableRegions(BukkitAdapter.adapt(location))
            .mapNotNull { conf.region2songs[it.id] }
    }


    suspend fun playSongIfNotPlaying(songName: String, player: Player) {
        val song = conf.songsByKey[songName] ?: return
        player.toGeary().apply {
            if (has<NowPlaying>()) return
            set(NowPlaying(song, System.currentTimeMillis()))
            val recents = getOrSet { RecentlyPlayed(setOf()) }
            set(RecentlyPlayed(recents.songs + songName))
        }
        player.playSound(player, song.key, song.category, song.volume, song.pitch)
        delay(song.duration)
        if (player.isConnected) player.toGeary().remove<NowPlaying>()
    }

    private fun chooseTimeBetween(start: Duration, end: Duration): Duration {
        return (start.inWholeSeconds..end.inWholeSeconds).random().seconds
    }
}
