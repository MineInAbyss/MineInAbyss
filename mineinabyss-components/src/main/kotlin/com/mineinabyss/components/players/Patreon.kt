package com.mineinabyss.components.players

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Month

@Serializable
@SerialName("mineinabyss:patreon")
class Patreon(
    var tier: Int = 1,
    var kitUsedMonth: Month? = null
)
