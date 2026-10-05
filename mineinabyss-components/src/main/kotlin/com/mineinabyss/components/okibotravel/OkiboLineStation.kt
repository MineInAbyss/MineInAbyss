package com.mineinabyss.components.okibotravel

import com.mineinabyss.idofront.serialization.LocationAltSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Location

/** Fractions of the okibo map image from its top left corner, both 0..1, written as "x,y" */
@Serializable(with = MapPosition.Serializer::class)
data class MapPosition(val x: Double, val y: Double) {
    object Serializer : KSerializer<MapPosition> {
        override val descriptor = PrimitiveSerialDescriptor("MapPosition", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: MapPosition) = encoder.encodeString("${value.x},${value.y}")

        override fun deserialize(decoder: Decoder): MapPosition = decoder.decodeString().split(",").map { it.trim().toDouble() }
            .also { require(it.size == 2) { "Okibo map positions are written as x,y" } }
            .let { (x, y) -> MapPosition(x, y) }
    }
}

@Serializable
@SerialName("mineinabyss:okibo_line_station")
data class OkiboLineStation(
    val id: String,
    val displayName: String,
    val location: @Serializable(with = LocationAltSerializer::class) Location,
    val map: MapPosition? = null,
)
