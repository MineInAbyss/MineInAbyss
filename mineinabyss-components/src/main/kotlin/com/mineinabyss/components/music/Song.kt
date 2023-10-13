package com.mineinabyss.components.music

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Song(
    val sound: String,
    val duration: Duration,
)
