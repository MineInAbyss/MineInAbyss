package com.mineinabyss.components.gondolas

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.Serializable

@Serializable
@AutoscanComponent
class UnlockedGondolas(
    val keys: MutableList<String> = mutableListOf()
)
