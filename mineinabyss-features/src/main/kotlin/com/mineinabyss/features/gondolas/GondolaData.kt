package com.mineinabyss.features.gondolas

import com.mineinabyss.components.gondolas.Gondola

data class GondolaData(
    val id: String,
    val gondola: Gondola,
    val type: GondolaType
)
