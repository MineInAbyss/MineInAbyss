package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboMap
import kotlinx.serialization.Serializable

@Serializable
data class OkiboTravelConfig(
    val okiboStations: Set<OkiboLineStation> = setOf(),
    val okiboMaps: Set<OkiboMap> = setOf(),
    val costPerKM: Double = 1.0
) {
    val allStations get() = okiboStations.toMutableList().apply { addAll(okiboStations.map { it.subStations }.flatten()) }
}
