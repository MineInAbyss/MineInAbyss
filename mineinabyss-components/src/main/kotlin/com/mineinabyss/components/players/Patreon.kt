package com.mineinabyss.components.players

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:patreon")
class Patreon(
    var tier: Int = 1,
    var kitUsedStatus: Boolean = false,
)