package com.mineinabyss.components.okibotravel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("mineinabyss:okibo_traveler")
data class OkiboTraveler(val mainStation: String) {
    fun costTo(station: OkiboLineStation, stations: Set<OkiboLineStation>): Int? {
        val mainStation = stations.find { it.name == mainStation } ?: stations.first()
        val startIndex = stations.indexOf(mainStation)
        val endIndex = stations.indexOf(station)

        if (startIndex == -1 || endIndex == -1) return null
        if (startIndex < endIndex) return endIndex - startIndex
        return stations.size - startIndex + endIndex
    }
}

