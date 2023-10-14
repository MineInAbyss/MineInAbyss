package com.mineinabyss.components.music

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Song(
    val key: String,
    val duration: Duration,
    @YamlComment("WorldGuard regions this song can play in (can play anywhere if emtpy)")
    val regions: List<String>,
)
