@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.features.music

import kotlinx.serialization.UseSerializers
import com.charleskorn.kaml.YamlComment
import com.mineinabyss.components.music.Song
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.SoundCategory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
class MusicConfig(
    @YamlComment(
        "The min/max amount of time to wait before playing a song after once finishes",
        "Default is 10-20 minutes in Minecraft."
    )
    val minSongWaitTime: Duration = 10.minutes,
    val maxSongWaitTime: Duration = 20.minutes,
    @YamlComment("How long to wait before playing a song after a player logs in")
    val minWaitTimeOnLogin: Duration = 2.minutes,
    val maxWaitTimeOnLogin: Duration = 5.minutes,
    @YamlComment("A list of song definitions")
    val songs: List<Song> = listOf(Song(
            "mineinabyss:music.custom.layer5.beneath_the_ice",
            4.minutes,
            SoundCategory.AMBIENT,
            1f, 1f,
            listOf("layerfive")
        )
    )
) {
    @Transient
    val songsByKey = songs.associateBy { it.key }

    @Transient
    val region2songs = songsByKey.flatMap { (name, song) ->
        song.regions.map { region -> region to name }
    }.toMap()
}
