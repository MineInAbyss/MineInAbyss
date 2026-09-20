package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import com.mineinabyss.components.okibotravel.OkiboMap
import com.mineinabyss.idofront.serialization.DurationSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
data class OkiboTravelConfig(
    val okiboStations: List<OkiboLineStation> = listOf(),
    val okiboMaps: List<OkiboMap> = listOf(),
    val costPerKM: Double = 1.0,
    /** Width and height of a station's clickable dot on a map */
    val hitboxSize: Double = 0.15,
    /** How long a picked destination stays selected before a player has to click it again */
    val confirmTimeout: @Serializable(DurationSerializer::class) Duration = 5.seconds,
) {
    private val stationsById = okiboStations.associateBy(OkiboLineStation::id)

    fun station(id: String) = stationsById[id]

    fun destinationsOn(map: OkiboMap) = map.destinations.mapNotNull(::station)

    init {
        require(stationsById.size == okiboStations.size) { "Duplicate okibo station ids in config" }
        okiboMaps.forEach { map ->
            require(map.station in stationsById) { "Okibo map at ${map.location} is at unknown station ${map.station}" }
            map.destinations.forEach {
                require(it in stationsById) { "Okibo map ${map.station} points to unknown station $it" }
            }
        }
    }
}
