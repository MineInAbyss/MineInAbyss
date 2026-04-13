package com.mineinabyss.features.music

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.components.music.NowPlaying
import com.mineinabyss.components.music.RecentlyPlayed
import com.mineinabyss.components.music.ScheduledMusicJob
import com.mineinabyss.deeperworld.world.section.section
import com.mineinabyss.extracommands.commands.isAfk
import com.mineinabyss.features.abyss
import com.mineinabyss.features.helpers.layer
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.idofront.util.randomOrMin
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player

class MusicScheduler(
    val conf: MusicConfig,
) {
    fun stopSchedulingMusic(player: Player) {
        player.toGeary().apply {
            get<ScheduledMusicJob>()?.job?.cancel()
            remove<NowPlaying>()
            remove<ScheduledMusicJob>()
        }
    }

    fun scheduleMusicPlaying(player: Player) {
        val musicPlayingJob = abyss.launch {
            val waitOnLogin = conf.waitTimeOnLogin.randomOrMin()
            abyss.logger.i("Starting music scheduler for ${player.name}, waiting $waitOnLogin before playing.")
            delay(waitOnLogin)
            while (player.isConnected) {
                val playable = getPlayableSongsAtLocation(player.location)
                if (playable.isEmpty()) delay(conf.songWaitTime.endInclusive)
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
                        if (player.isAfk) return@let conf.songWaitTime.randomOrMin()
                        playSongIfNotPlaying(song, player)

                        // Choose a random wait time as defined in config
                        conf.songWaitTime.randomOrMin().also {
                            abyss.logger.i("Finished playing $song, waiting $it before playing another.")
                        }
                    } ?: conf.songWaitTime.endInclusive.also {
                        abyss.logger.i("No songs to play, waiting $it before trying again.")
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
}
