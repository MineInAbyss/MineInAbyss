package com.mineinabyss.components.unlocks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ids unlocked through dialogs, like paid access to a gondola */
@Serializable
@SerialName("mineinabyss:player_unlocks")
class PlayerUnlocks(
    val keys: MutableSet<String> = mutableSetOf()
)
