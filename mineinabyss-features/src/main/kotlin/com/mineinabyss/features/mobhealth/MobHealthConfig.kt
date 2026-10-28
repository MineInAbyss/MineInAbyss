package com.mineinabyss.features.mobhealth

import com.charleskorn.kaml.YamlComment
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.time.ticks
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
class MobHealthConfig(
    val enabled: Boolean = false,
    @YamlComment("Emojy emotes the bar is drawn with", "Fill and trail are sheets with one row per pixel of health, from empty to full")
    val frameEmote: String = "mob_health_frame",
    val fillEmote: String = "mob_health_fill",
    val trailEmote: String = "mob_health_trail",
    @YamlComment("Pixels between the left edge of the frame and the fill, and between the fill and the right edge")
    val fillInset: Int = 19,
    val fillPadding: Int = 6,
    @YamlComment("How long a player keeps seeing the bar after their last hit")
    @Serializable(DurationSerializer::class) val visibleDuration: Duration = 120.ticks,
)
