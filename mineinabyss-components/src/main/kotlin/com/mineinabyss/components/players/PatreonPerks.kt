package com.mineinabyss.components.players

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:patreon_perks")
data class PatreonPerks(
    val emote: String? = null,
    /** [java.time.YearMonth] in ISO format, e.g. `2026-10` */
    val tokenRedeemedMonth: String? = null,
)
