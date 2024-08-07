package com.mineinabyss.features.music

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.music.NowPlaying
import com.mineinabyss.components.music.RecentlyPlayed
import com.mineinabyss.components.music.ScheduledMusicJob
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object MusicScheduler {
    private val conf get() = Features.music.config

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
            abyss.logger.i("Starting music scheduler for ${player.name}, waiting $waitOnLogin before playing.")
            delay(waitOnLogin)
            while (player.isConnected) {
                val playable = getPlayableSongsAtLocation(player.location)
                if (playable.isEmpty()) delay(conf.maxSongWaitTime)
                else {
                    val recentlyPlayed = player.toGeary().getOrSet<RecentlyPlayed> { RecentlyPlayed(setOf()) }
                    val notRecentlyPlayed = playable.filter { it !in recentlyPlayed.songs }
                    abyss.logger.i("Recently played: $recentlyPlayed")
                    abyss.logger.i("Playable: $playable")
                    val chooseFrom = notRecentlyPlayed.takeIf { notRecentlyPlayed.isEmpty() }?.apply {
                        player.toGeary().set(RecentlyPlayed(setOf()))
                    } ?: playable
                    val song = chooseFrom.randomOrNull()
                    abyss.logger.i("Playing $song")

                    delay(song?.let {
                        playSongIfNotPlaying(song, player)

                        // Choose a random wait time as defined in config
                        val wait = chooseTimeBetween(conf.minSongWaitTime, conf.maxSongWaitTime)
                        abyss.logger.i("Finished playing $song, waiting $wait before playing another.")
                        wait
                    } ?: run {
                        abyss.logger.i("No songs to play, waiting ${conf.maxSongWaitTime} before trying again.")
                        conf.maxSongWaitTime
                    })
                }
            }
        }
        player.toGeary().set(ScheduledMusicJob(musicPlayingJob))
    }

    fun getPlayableSongsAtLocation(location: Location): List<String> {
        return conf.songs.filter { location.layer?.id in it.regions || location.section?.name in it.regions }
            .plus(conf.songs.filter { it.regions.isEmpty() })
            .distinct().map { it.key }
    }


    suspend fun playSongIfNotPlaying(songName: String, player: Player) {
        val song = conf.songsByKey[songName] ?: return
        player.toGeary().apply {
            if (has<NowPlaying>()) return
            set(NowPlaying(song, System.currentTimeMillis()))
            val recents = getOrSet<RecentlyPlayed> { RecentlyPlayed(setOf()) }
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
