@file:UseSerializers(DurationSerializer::class)

package com.mineinabyss.features.music

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.components.music.Song
import com.mineinabyss.idofront.serialization.DurationRangeSerializer
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.util.DurationRange
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import org.bukkit.SoundCategory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
class MusicConfig(
    @YamlComment("The min/max amount of time to wait before playing a song after once finishes", "Default is 10..20 minutes in Minecraft.")
    val songWaitTime: @Serializable(DurationRangeSerializer::class) DurationRange = 5.minutes..10.minutes,
    @YamlComment("How long to wait before playing a song after a player logs in")
    val waitTimeOnLogin: @Serializable(DurationRangeSerializer::class) DurationRange = 2.minutes..5.minutes,
    @YamlComment(
        "A list of song definitions, options are:",
        "key: The resourcepack key for this song",
        "duration: How long the song lasts",
        "category: The sound category to play the song in (default is MUSIC)",
        "volume: The volume of the song (default is 1)",
        "pitch: The pitch of the song (default is 1)",
        "regions: Layers & sections this song can play in (can play anywhere if empty)"
    )
    val songs: List<Song> = listOf(
        Song(
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
}
