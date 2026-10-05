package com.mineinabyss.features.okibotravel

import com.mineinabyss.components.okibotravel.OkiboLineStation
import kotlinx.serialization.Serializable
import com.mineinabyss.idofront.serialization.VectorAsListSerializer
import org.bukkit.Location
import org.bukkit.util.Vector

@Serializable
data class OkiboTravelConfig(
    val okiboStations: List<OkiboLineStation> = listOf(),
    val costPerKM: Double = 1.0,
    /** Nexo furniture ids that open the okibo map, geary prefabs get theirs as namespace_key */
    val noticeboards: List<String> = listOf("mineinabyss_noticeboard_okibo"),
    val boardRange: Double = 16.0,
    val okiboModel: OkiboModelConfig = OkiboModelConfig(),
    val mapFace: MapFaceConfig = MapFaceConfig(),
) {
    private val stationsById = okiboStations.associateBy(OkiboLineStation::id)

    fun station(id: String) = stationsById[id]

    fun stationNear(location: Location): OkiboLineStation? = okiboStations
        .filter { it.location.world == location.world }
        .minByOrNull { it.location.distanceSquared(location) }
        ?.takeIf { it.location.distanceSquared(location) <= boardRange * boardRange }

    init {
        require(stationsById.size == okiboStations.size) { "Duplicate okibo station ids in config" }
    }
}

/** Sizes are in blocks at scale 1 */
@Serializable
data class OkiboModelConfig(
    val blueprint: String = "okibo_train",
    val walkAnimation: String = "walk",
    val bodyBone: String = "body",
    val length: Double = 3.8,
    val width: Double = 3.6,
    val riderTop: Double = 3.45,
    val bodyPivot: Double = 0.625,
    val saddle: Double = 1.6,
)

/** from and to of the noticeboard model element whose north face shows the map */
@Serializable
data class MapFaceConfig(
    val from: @Serializable(VectorAsListSerializer::class) Vector = Vector(2.0, 6.48, 3.6175),
    val to: @Serializable(VectorAsListSerializer::class) Vector = Vector(14.96, 19.44, 4.4275),
)
