package com.mineinabyss.components.music

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.EncodeDefault.Mode.NEVER
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.bukkit.SoundCategory
import kotlin.time.Duration

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Song(
    val key: String,
    val duration: @Serializable(with = DurationSerializer::class) Duration,
    @EncodeDefault(NEVER) val category: SoundCategory = SoundCategory.MUSIC,
    @EncodeDefault(NEVER) val volume: Float = 1f,
    @EncodeDefault(NEVER) val pitch: Float = 1f,
    @YamlComment("WorldGuard regions this song can play in (can play anywhere if emtpy)")
    val regions: List<String>,
)
