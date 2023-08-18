package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboMap
import kotlinx.serialization.Serializable

@Serializable
data class OkiboTravelConfig(
    val okiboStations: Set<OkiboLineStation>,
    val okiboMaps: Set<OkiboMap>,
    val costPerKM: Double
)
