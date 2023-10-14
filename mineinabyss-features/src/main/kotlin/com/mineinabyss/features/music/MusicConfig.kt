package com.mineinabyss.features.music

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.components.music.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    @YamlComment("A list of song definitions")
    val songs: Map<String, Song> = mapOf(
        "default" to Song(
            "mineinabyss:music.custom.layer5.beneath_the_ice",
            4.minutes,
            listOf("layer5")
        )
    ),
) {
    @Transient
    val region2songs = songs.flatMap { (name, song) ->
        song.regions.map { region -> region to name }
    }.toMap()
}
