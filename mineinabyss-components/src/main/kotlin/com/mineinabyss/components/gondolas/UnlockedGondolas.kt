package com.mineinabyss.components.gondolas

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("mineinabyss:unlocked_gondolas")
@AutoscanComponent
class UnlockedGondolas(
    val keys: MutableList<String> = mutableListOf()
)
