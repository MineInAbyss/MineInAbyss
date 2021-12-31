package com.mineinabyss.components.gondolas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:unlocked_gondolas")
class UnlockedGondolas(
    val keys: MutableList<String> = mutableListOf()
)
